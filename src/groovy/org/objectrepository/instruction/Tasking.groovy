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

    private int _total = -1

    List getTasks() {
        def program = (plan) ?: parent.plan
        program.collect {
            def controllerAction = Pattern.compile("([A-Z])").matcher(it).replaceAll(" \$1").trim().toLowerCase().split("\\s", 2)
            final String collection = controllerAction[0]
            int total = (_total == -1) ? mongo.getDB('sa')."$collection".count([fileSet: fileSet]) : _total
            int working = mongo.getDB('sa')."$collection".count([fileSet: fileSet, workflow: [$elemMatch: ['name': it, 'statusCode': [$lt: 700]]]])
            int failed = mongo.getDB('sa')."$collection".count([fileSet: fileSet, workflow: [$elemMatch: ['name': it, 'statusCode': [$gt: 699, $lt: 800]]]])
            int success = mongo.getDB('sa')."$collection".count([fileSet: fileSet, workflow: [$elemMatch: ['name': it, 'statusCode': [$gt: 799]]]])
            int processed = success + failed
            [name: it, total: total, working: working, success: success, failed: failed, processed: processed]
        }
    }

    boolean getIngesting() {
        task.name == 'InstructionIngest' && task.statusCode == 800
    }
}
