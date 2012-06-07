package org.objectrepository.instruction

import org.objectrepository.util.OrUtil
import java.util.regex.Pattern

/**
 * Tasking
 *
 * Profiles the getter and setter and accessibility for the primary task in a workflow
 */
abstract class Tasking {

    def mongo
    def cacheTask = null

    Task getTask() {
        OrUtil.takeFirst(workflow)
    }

    void setTask(def map) {
        setTask(new Task(map.properties))
    }

    void setTask(Task _task) {
        if (_task.name) {
            workflow.push(_task)
        }
    }

    List<Task> getTasks() {
        def program = (plan) ?: parent.plan
        def s = program.collect {
            def controllerAction = Pattern.compile("([A-Z])").matcher(it).replaceAll(" \$1").trim().toLowerCase().split("\\s", 2)
            final String collection = controllerAction[0]
            int total = mongo.getDB('sa')."$collection".count([fileSet: fileSet])
            int working = mongo.getDB('sa')."$collection".count([fileSet: fileSet, 'workflow.statusCode': [$lt: 700]])
            int success = mongo.getDB('sa')."$collection".count([fileSet: fileSet, 'workflow.statusCode': [$gt: 799]])
            int failed = total - working - success
            int processed = total - working
            [name: it, total: total, working: working, success: success, failed: failed, processed:processed]
        }
        s
    }

    def ingesting = {
        task.name == 'InstructionIngest' && task.statusCode == 800
    }
}
