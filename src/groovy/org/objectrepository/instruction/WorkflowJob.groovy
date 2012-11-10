package org.objectrepository.instruction

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.WriteResult
import grails.converters.XML
import org.apache.camel.CamelExecutionException
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.objectrepository.util.OrUtil

/**
 * WorkflowJob
 *
 * Iterates over the home directories.
 *
 * General, task agnostic decisions are placed here.
 * For task specific logic see WorkflowActiveService
 */
abstract class WorkflowJob {

    static transactional = "mongo"
    def mongo
    def grailsApplication
    File home
    TaskValidationService taskValidationService
    def taskProperties

    static int TASK_FREEZE = 797
    static long messageTTL = 3600000 // One hour of queueing status. And then the task becomes stale.

    public WorkflowJob() {
        taskProperties = new DefaultGrailsDomainClass(Task.class).persistentProperties.collect {
            it.name
        }
    }

    /**
     * runMethod
     *
     * Runs a method that is dynamically called via the current workflow status.
     * There are two ways of calling a method dynamically.. In the workflow configuration use:
     *
     * 1. methods specified in the element action=[method,...]. E.g. action=['next')
     * This will execute methods next(document)
     *
     * 2. Workflow name convention:
     * [WorkflowName]
     * [WorkflowName][StatusCode]
     * task[WorkflowName]
     * [objectClass][StatusCode]
     * If not we fall back on a more general statusCode as a method: task[InstanceType][StatusCode]
     * If that does not exist either, we fall back on the most generic task: task[StatusCode]
     *
     * @param document
     * @return
     */
    void runMethod(def document) {

        if (!document.task) {
            log.warn id(document) + "This document has no task."
            return
        }

        def method = methodName(document)
        def current = currentTask(document)
        runMethod(document, method)
        if (taskValidationService.hasChange(current, document)) {
            runMethod(document)
        }
        else {
            saveWorkflow(document)
        }
    }

    /**
     * methodName
     *
     * Construct a method name from z current task.
     * [WorkflowName] or [WorkflowName][StatusCode] or task[WorkflowName]
     * If not we fall back on a more general statusCode as a method: task[InstanceType][StatusCode]
     * If that does not exist either, we fall back on the most generic task: task[StatusCode]
     *
     * @param m
     * @return
     */
    private String methodName(def document) {
        def postFix = (document.task.statusCode == 0) ? "" : document.task.statusCode
        def list = [plans[document.task.name]?.statusCodes[document.task.statusCode]?.action,
                document.task.name + postFix,  // e.g. SomeTask123
                document.getClass().simpleName + document.task.name + postFix, // e.g. SomeClassSomeTask123
                document.getClass().simpleName + postFix, // SomeClass123
                "task" + postFix]   // e.g. task800
        list.find {
            it && delegate.metaClass.respondsTo(this, it, document)
        }
    }

    /**
     * runMethod
     *
     * @param document
     * @param method
     * @return
     */
    @SuppressWarnings("GroovyAssignabilityCheck")
    void runMethod(def document, String method) {
        if (method) {
            log.info id(document) + "Method " + method + ": " + plans[document.task.name].statusCodes[document.task.statusCode]?.purpose
            "$method"(document)
        }
        else {
            final task = plans[method]?.statusCodes
            if (task) {
                final map = [task: [name: method]]
                first(map)
                if (taskValidationService.hasChange(map, document)) {
                    changeWorkflow(method, document)
                }
            }
        }
    }

    /**
     * changeWorkflow
     *
     * Migrates to a different task. If the task statusCode is absent, we default to the first task in the workflow
     *
     * @param taskName
     * @param document
     * @return
     */
    void changeWorkflow(String taskName, def document) {
        String old = document.task.name + document.task.statusCode
        def _statusCode = taskName.substring(taskName.size() - 3)
        String name = _statusCode.isNumber() ? taskName.substring(0, taskName.size() - 3) : taskName
        int statusCode = _statusCode.isNumber() ? _statusCode.toInteger() : firstStatusCode(name)
        Task task = new Task(name: name, statusCode: statusCode)
        plans[name].task?.each {
            task."$it.key" = it.value
        }
        document.workflow.add(0, task)
        log.info id(document) + "Changed workflow from " + old + " to " + document.task.name + document.task.statusCode
    }

    /**
     * nextWorkflow
     *
     * Moves a new task to the bottom ( beginning at index 0... depending on the metaphor ) of the workflow.
     * This way the task becomes active.
     * In case that last task is already finished ( statusCode > 799 ) we move on again to the next, etc.
     *
     *
     * @param document
     */
    void nextWorkflow(def document) {
        final String old = document.task.name + document.task.statusCode
        boolean next = true // The do-while loop is not yet supported in this groovy version
        final String currentTaskName = document.task.name
        while (next) {
            document.workflow << document.workflow.remove(0)
            if (currentTaskName == document.task.name) return
            next = (document.task.statusCode > 799 && document.task.name != 'EndOfTheRoad')
        }
        first(document)
        log.info id(document) + "nextWorkflow from " + old + " to " + document.task.name + document.task.statusCode
    }

    /**
     * Moves the workflow up ( or down ? ) one step: incremental. Like from 100 to 110
     *
     * @param document
     * @return
     */
    void next(def document) {
        int statusCode = document.task.statusCode
        plans[document.task.name].statusCodes.reverseEach {
            statusCode = (it.key > document.task.statusCode) ? it.key : statusCode
        }
        document.task.statusCode = statusCode
        log.info id(document) + "Moved task to next: " + document.task.name + ":" + document.task.statusCode
    }

    /**
     * Moves the current statusCode of this task to its first setting
     *
     * @param document
     * @return
     */
    void first(def document) {
        document.task.statusCode = firstStatusCode(document.task.name)
        log.info id(document) + "Moved task to first: " + document.task.name + ":" + document.task.statusCode
    }

    private int firstStatusCode(String name) {
        def list = plans[name].statusCodes.collect {
            it.key
        }
        list.first()
    }

    /**
     * Moves the current statusCode to the end of the task
     *
     * @param document
     */
    void last(def document) {
        def list = plans[document.task.name].statusCodes.collect {
            it.key
        }
        document.task.statusCode = list.last()
        log.info id(document) + "Moved task to last: " + document.task.name + ":" + document.task.statusCode
    }

    /**
     * retry
     *
     * A task was not completed for whatever reason. Repeat it by setting the statusCode and try again.
     * The number of attempts is restricted by the task.limit. De limit's default is set in the Task class
     * and can be set in the PlanConfig
     *
     * If the attempt keeps failing and the limit is reached, we proceed to the next task
     *
     * @param document
     */
    void retry(def document) {

        if (document.task.exitValue == 230) {
            log.info id(document) + "Freezing task. Severe problem and should not continue."
            document.workflow.each {
                it.statusCode = (it.statusCode < 800) ? TASK_FREEZE : it.statusCode
            }
        }
        else if (document.task.exitValue == 240) {
            log.info id(document) + "Skipping task. The document has an unknown property making it incompatible with this service."
            document.task.statusCode = 798
            nextWorkflow(document)
        }
        else if (document.task.exitValue == 245) {
            log.info id(document) + "Skipping task. The document cannot proceed because the required files are not there."
            document.task.statusCode = 800
            nextWorkflow(document)
        }
        else if (document.task.exitValue == 250) {
            log.info id(document) + "Skipping task. It's design cannot handle this type of document."
            document.task.statusCode = 800
            nextWorkflow(document)
        }
        else if (++document.task.attempts > document.task.limit) {
            log.info id(document) + "Failed. Tried " + document.task.limit + " times."
            document.task.statusCode = 790
            nextWorkflow(document)
        }
        else {
            final String info = id(document) + "Retry task " + document.task.attempts + "\"" + document.task.limit + " " + document.task.name
            log.info info
            first(document)
        }
    }

    /**
     * addUpdateUpsertOrDelete
     *
     * The action indicator determines the flow of the file.
     * Where:
     * action=delete : we move to the delete queue
     * action=add : we go to the next phase
     * action=update : with file location, we proceed like add.
     * action=upsert : no location. A simple metadata update
     *
     * Sets the expected tasks.
     *
     * @param document
     * @return
     */
    def Start100(def document) {

        OrUtil.setInstructionPlan(document.parent)
        plans.each { plan ->  // Iterating from the config plan will ensure the correct ordering of tasks.
            def wf = document.parent.plan.find() {
                it == plan.key
            }
            if (wf) {
                def attributes = [name: wf, info: "Default workflow"]
                plan.value.task?.each() {
                    attributes << it
                }
                document.workflow << new Task(attributes)
                log.info id(document) + "Added task " + attributes.name

                plan.value.methods?.each {
                    if (it.value) "$it.key"(it.value, document) else "$it.key"(document)
                }
            }
        }

        document.workflow << new Task(name: 'EndOfTheRoad', info: "Default workflow")
        next(document) // we just go through the mill here. Atomic updates for access should go via the controller
    }

    /**
     * executeBefore
     *
     * This task should and complete and then run a system task.
     *
     * @param document
     * @param args
     * @return
     */
    def executeBefore(def args, def document) {
        def attributes = [name: args, info: "System workflow"]
        log.info id(document) + "Added executeAfter task " + attributes.name
        document.workflow << new Task(attributes)
    }

    /**
     * executeAfter
     *
     * This task should wait until a system task is complete.
     *
     * @param document
     * @param args
     * @return
     */
    def executeAfter(def args, Stagingfile document) {
        def attributes = [name: args, info: "System workflow"]
        log.info id(document) + "Added executeAfter task " + attributes.name
        def task = document.workflow.remove(document.workflow.size() - 1)
        document.workflow << new Task(attributes)
        document.workflow << task
    }

    /**
     * Changes the name of the current queue based on the Content Type.
     * For example: image/jpeg appends 'Image' to the name.
     *
     * @param document
     */
    def renameQueueWithContentType(def document) {

        final String type = OrUtil.camelCase([document.contentType.split('/', 2)[0]])
        switch (type) {
            case 'Audio':
            case 'Image':
            case 'Video':
                log.info id(document) + "renameQueueWithContentType " + type
                document.workflow.last().queue = document.workflow.last().name + type
                break;
        }
    }

    /**
     * InstructionRetry100
     *
     * Resets all failed tasks to 100 for a re-run.
     *
     * @param document
     */
    def InstructionRetry100(def document) {
        mongo.getDB('sa').stagingfile.find(
                fileSet: document.fileSet, 'workflow.statusCode': [$lt: 800]
        ).each {
            Stagingfile stagingfile = it as Stagingfile
            stagingfile.parent = document
            stagingfile.workflow.each {
                it.statusCode = (it.statusCode > 699 && it.statusCode < 800) ? 100 : it.statusCode
            }
            stagingfile.workflow.remove(stagingfile.workflow.find {it.name == 'EndOfTheRoad'})
            stagingfile.workflow << new Task(name: 'EndOfTheRoad', info: "Default workflow")
            save(stagingfile) // we just go through the mill here.
        }
        changeWorkflow('InstructionIngest800', document)
    }

    void task100(def document) {
        next(document)
    }

    void task200(def document) {
        message(document)
    }

    /**
     * task300
     *
     * In case the message is expired we will reset all.
     *
     * @param document
     */
    void task300(def document) {
        final Date expired = OrUtil.expirationDate(messageTTL)
        if (document.task?.end < expired) {
            first(document)
        }
    }

    /**
     * task400
     *
     * In case the worker node has become unresponsive we skip so we can evaluate.
     *
     * @param document
     */
    void task400(def document) {
        final Date expired = OrUtil.expirationDate(messageTTL)
        if (document.task?.end < expired) {
            next(document)
        }
    }

    /**
     * task500
     *
     * Walk to the next step
     *
     * @param document
     */
    void task500(def document) {
        next(document)
    }

    /**
     * task600
     *
     * Proceed when successful. Or else just retry.
     *
     * @param document
     */
    void task600(def document) {
        if (document.task.exitValue == 0) {
            last(document)
        } else {
            retry(document)
        }
    }

    /**
     * Instruction800
     *
     * When an instruction task is at the end of the job, we see if we should progress.
     *
     * @param document
     */
    void Instruction800(def document) {
        if (!document.ingesting && document.autoIngestValidInstruction && taskValidationService.hasValidDBInstruction(document)) {
            changeWorkflow('InstructionIngest', document)
        }
    }

    /**
     * StagingfileEndOfTheRoad800
     *
     * Done with workflow.
     * There ought to be no failures.
     *
     * @param document
     * @return
     */
    def StagingfileEndOfTheRoad800(def document) {

        document.task.statusCode = (document.workflow.find {
            it.statusCode < 800 // See the retry method for acceptable and not so acceptable failures
        }) ? 850 : 900
    }

    /**
     * message
     *
     * Places a message on the queue. It writes an InstructionType into a XML message as text
     * This will be for instruction and files.
     *
     * The name of the queue is the same as the task. For the derivative tasks the message may be redirected
     * by appending the contentType's type to it.
     *
     * We want to be sure the task identifier is in the database before the message queue client
     * receives it. Hence here we save.
     *
     * The message will expire given the messageTTL setting minus a minute offset
     *
     * @param instructionInstance
     * @return
     */
    void message(def document) {

        document.task.taskKey()
        if (save(document)) {
            try {
                final String queue = (document.task.queue) ?: document.task.name
                sendMessage(["activemq", queue].join(":") + "?timeToLive=" + messageTTL, OrUtil.makeOrType(document))
                log.info id(document) + "Send message to queue " + queue
                next(document)
            } catch (Exception e) {
                // message queue may be down
                log.warn e.message
            }
        }
    }

    def currentTask(def document) {
        [task: [name: document.task.name, statusCode: document.task.statusCode]]
    }

    String id(def document) {
        final String file = (document instanceof Instruction) ? document.fileSet : document.location;
        document.id.toString() + ":" + file + "\n"
    }

    /**
     * saveWorkflow
     *
     * Commits the new workflow, and copies the task to the tasks history collection.
     * If the new task is in fact identical to that of the current one, we will not bother to persist.
     *
     * @param document
     * @return
     */
    boolean saveWorkflow(def document) {
        if (!document.fileSet || document.task.name == document.cacheTask?.name &&
                document.task.statusCode == document.cacheTask?.statusCode) {
            log.info id(document) + "Skipping save for workflow status... no changes"
            true
        } else {
            log.info id(document) + "Saving task."
            save(document)
        }
    }

    /**
     * save
     *
     * update to persist the workflow.
     * All tasks in the workflow will have a map plus index n explicit set, as we cannot
     * serialize it to a map.
     *
     * Persist the workflow
     * @param document
     * @return
     */
    boolean save(def document) {

        final collection = mongo.getDB('sa').getCollection(document.class.getSimpleName().toLowerCase())
        int n = 0
        def workflow = document.workflow?.collect { task ->
            task.n = n++
            taskProperties.inject([:]) { init, key ->
                init.putAt(key, task."$key")
                init
            }
        }
        document.task?.end = new Date()
        if (result(collection.update([_id: document.id], [$set: [workflow: workflow]]))) {
            if (document.task.statusCode == firstStatusCode(document.task.name)) {
                sendMessage('activemq:status', document.task.identifier)
            }
            return true
        }
        false
    }

    boolean delete(def document) {

        final DBCollection collection = mongo.getDB('sa').getCollection(document.class.getSimpleName().toLowerCase())
        result(collection.remove(new BasicDBObject("_id", document.id)))
    }

    boolean result(WriteResult result) {
        if (result.error) {
            log.error "Failure when processing document: " + result.error
        }
        result.error == null
    }

    void exception(def document, Exception e) {

        println("Failure when processing document: " + e.message)
        if (e instanceof CamelExecutionException) return // The message queue is offline
        println(document as XML)
        e.stackTrace?.each {
            println(it)
        }
    }

    def getHome() {

        if (!this.@home) {
            File entry = new File(grailsApplication.config.sa.path as String) // home root directory
            if (!entry.isDirectory() || !entry.exists()) {
                log.fatal "${entry} is not a directory; or does not exist"
                System.exit(-1)
            }
            log.info "Staging area home directory set to " + entry
            log.info "I will now monitor the pattern: " + entry + "/[cpAdminFolder]/[cpUserFolder]/[fileSet]"
            home = entry
        }
        this.@home
    }

    def getPlans() {
        grailsApplication.config.plans
    }
}