package org.objectrepository.instruction

import org.objectrepository.util.OrUtil

/**
 * Tasking
 *
 * Profiles the getter and setter and accessibility for the primary task in a workflow
 */
abstract class Tasking {

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

}
