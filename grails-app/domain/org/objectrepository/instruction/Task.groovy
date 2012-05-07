package org.objectrepository.instruction

/**
 * Task
 *
 * Used as an embedded composite object at the Instruction and Stagingfile.

 * Used in the corresponding workflow to express a workflow state. A workflow being a sequence of tasks.
 *
 */
class Task {

    String identifier
    String name
    int statusCode = 0 // 10, 20, 30, etc.
    String info // event derived from statusCode
    Date start = new Date()
    Date end = new Date()
    int total = 0 // Total number of whatevers
    int processed = 0 // Total number of processed whatevers
    int attempts = 1
    int limit = 3 // Three attempts maximum before permanent failure. Default can be overwritten in WorkflowConfig
    int exitValue = Integer.MAX_VALUE // Undetermined

    static constraints = {
        identifier(nullable: true)
        info(nullable: true)
    }
    static mapping = {
        identifier index: false
    }

    static mapWith = "mongo"
}
