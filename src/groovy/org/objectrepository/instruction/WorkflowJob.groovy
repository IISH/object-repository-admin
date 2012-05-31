package org.objectrepository.instruction

import com.mongodb.WriteResult
import grails.converters.XML
import org.apache.camel.CamelExecutionException
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.objectrepository.util.OrUtil

/**
 * WorkflowJob
 *
 * Iterates over the home directories.
 */
abstract class WorkflowJob {

    static transactional = 'mongo'
    def mongo
    def grailsApplication
    File home
    TaskValidationService taskValidationService
    static def locked = [:]
    def taskProperties = []

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
            if (document instanceof Stagingfile) {
                def task = document.workflow.find {
                    it.name == document.task.name
                }
                if (task) {
                    task.statusCode = document.task.statusCode
                }
            }
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
    String methodName(def document) {
        def postFix = (document.task.statusCode == 0) ? "" : document.task.statusCode
        def list = [workflow[document.task.name]?.statusCodes[document.task.statusCode]?.action,
                document.task.name + postFix,
                document.getClass().simpleName + postFix,
                "task" + postFix]
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
            log.info id(document) + "Method " + method + ": " + workflow[document.task.name].statusCodes[document.task.statusCode]?.purpose
            "$method"(document)
        }
        else {
            final task = workflow[method]?.statusCodes
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
     * @param task
     * @param document
     * @return
     */
    void changeWorkflow(String task, def document) {
        def statusCode = task.substring(task.size() - 3)
        document.task.name = statusCode.isNumber() ? task.substring(0, task.size() - 3) : task
        document.task.statusCode = statusCode.isNumber() ? statusCode.toInteger() : -1
        if (document.task.statusCode == -1) first(document)
        document.task.attempts = 1
        document.task.limit = (workflow[document.task.name].task?.limit) ?: 3
        log.info id(document) + "Changed workflow: " + document.task.name + ":" + document.task.statusCode
    }

    /**
     * Moves the workflow up ( or down ? ) one step: incremental. Like from 100 to 110
     *
     * @param document
     * @return
     */
    void next(def document) {
        int statusCode = document.task.statusCode
        workflow[document.task.name].statusCodes.reverseEach {
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
        def list = workflow[document.task.name].statusCodes.collect {
            it.key
        }
        document.task.statusCode = list.first()
        log.info id(document) + "Moved task to first: " + document.task.name + ":" + document.task.statusCode
    }

    /**
     * Moves the current statusCode to the end of the task
     *
     * @param document
     */
    void last(def document) {
        def list = workflow[document.task.name].statusCodes.collect {
            it.key
        }
        document.task.statusCode = list.last()
        log.info id(document) + "Moved task to last: " + document.task.name + ":" + document.task.statusCode
    }

    /**
     * retry
     *
     * A task was not completed for whatever reason. Repeat it by setting the statusCode and try again.
     * The number of attempts is restricted by the task.limit
     *
     * If the attempt keeps failing, we proceed to the next task
     *
     * @param document
     */
    void retry(def document) {
        if (++document.task.attempts > document.task.limit) {
            final String info = id(document) + "Failed. Tried " + document.task.limit + " times."
            log.info info
            document.failed << new Task(name: document.task.name, info: info)
            last(document)
        }
        else {
            final String info = id(document) + "Retry task " + document.task.attempts + "\"" + document.task.limit + " " + document.task.name
            log.info info
            first(document)
        }
    }

    /**
     * failed
     *
     * Sets the workflow as failed.
     *
     * @param document
     */
    void failed(def document) {
        document.task.failure = true
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

            switch (document.action) {
                case 'delete':
                    changeWorkflow('FileRemove', document)
                    break
                case 'add':
                case 'update':
                case 'upsert':
                default:
                    document.workflow = [document.task]
                    document.failed = []
                    def plan = (document.parent.plan.size() == 0) ? document.parent.parent.plan : document.parent.plan
                    plan.each {
                        document.workflow << new Task(name: it)
                    }
                    next(document) // we just go through the mill here. Atomic updates for access should go via the controller
                    break
            }
        }

    void task100(def document) {
        next(document)
    }

    void task200(def document) {
        message(document)
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
     * Stagingfile600
     *
     * Proceed when successful. Or else just retry.
     * When we are fine, we'll update the workflow.
     *
     * @param document
     */
    void Stagingfile600(def document) {
        if (document.task.exitValue == 0) {

            // Update the staging file's workflow status:
            document.workflow.find {
                it.name == document.task.name
            }?.statusCode = 800

            // Followed by an incremental in the instruction
            document.parent.workflow.find {
                it.name == document.task.name
            }?.processed++

            document.parent.change = true
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
        if (document.task.name != 'InstructionIngest' && document.autoIngestValidInstruction && taskValidationService.hasValidDBInstruction(document)) {
            changeWorkflow('InstructionIngest', document)
        }
    }

    /**
     * EndOfTheRoad800
     *
     * Done with workflow.
     * There ought to be no failures.
     *
     * @param document
     * @return
     */
    def EndOfTheRoad800(def document) {

        if (document instanceof Stagingfile) {
            if (document.failed.size() == 0) {
                log.info id(document) + "Stagingfile successfull. Remove document."
                document.fileSet = null
                document.delete()
            } else {
                log.info id(document) + "Not all tasks are completed as we liked to. We leave it up to the enduser what to do with them."
                document.task.name = "Start"
                document.task.statusCode = 700
            }
        }
    }

    /**
     * Writes an InstructionType into XML and expresses that as text
     * This will be for instruction and files.
     *
     * We want to be sure the task identifier is in the database before the message queue client
     * receives it. Hence here we save.
     *
     * @param instructionInstance
     * @return
     */
    void message(def document) {

        document.task.taskKey()
        if (save(document)) {
            try {
                sendMessage(["activemq", document.task.name].join(":"), OrUtil.makeOrType(document))
                log.info id(document) + "Send message to queue"
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
            return true
        } else {
            log.info id(document) + "Saving task."
            document.task.end = new Date()
        }
        save(document)
    }

    boolean save(def document) {

        final collection = mongo.getDB('sa').getCollection(document.class.getSimpleName().toLowerCase())
        def _task = taskProperties.inject([:]) { init, key ->
            init.putAt(key, document.task."$key")
            init
        }
        def _workflow = document.workflow?.collect { w ->
            taskProperties.inject([:]) { init, key ->
                init.putAt(key, w."$key")
                init
            }
        }
        WriteResult result = collection.update([_id: document.id],
                [$set: [task: _task, workflow: _workflow]]
        )
        if (result.error) {
            println("Failure when processing document: " + result.error)
            println(document as XML)
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
            log.info "It will now monitor collect " + entry + "/cpAdminFolder/cpUserFolder/folders"
            home = entry
        }
        this.@home
    }

    def getWorkflow() {
        grailsApplication.config.workflow
    }
}