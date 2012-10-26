package org.objectrepository.instruction

/**
 * Task
 *
 * Used as an embedded composite object at the Instruction and Stagingfile.

 * Used in the corresponding workflow to express a workflow state. A workflow being a sequence of tasks.
 *
 */
class Task {

    int n = 0 // index: absolute position of this task in the workflow, starting from zero
    String identifier
    String name
    int statusCode = 0 // 10, 20, 30, etc.
    String info // event derived from statusCode
    Date start = new Date()
    Date end = new Date()
    int total = 0 // Total number of whatevers
    int processed = 0 // Total number of processed whatevers
    int attempts = 1
    int limit = 3 // Three attempts maximum before permanent failure. Default can be overwritten in PlanConfig
    int exitValue = Integer.MAX_VALUE // Undetermined
    String queue

    public String taskKey() {
        identifier = UUID.randomUUID().toString()
    }

    /**
     * getTime
     *
     * For instructions a running time would be <900
     * For staging files there is no such 900 code. The marker would be 800
     *
     * @param format
     * @return
     */
    public String getTime(String format = "Duration: %02d:%02d:%02d") {

        long time = (marker.getTime() - start.getTime()) / 1000;
        def s = time % 60;
        final i = time / 60 as Integer
        def m = i % 60;
        def h = (time / 3600) as Integer;
        String.format(format, h, m, s);
    }

    static constraints = {
        identifier(nullable: true)
        info(nullable: true)
        queue(nullable: true)
    }
    static mapping = {
        identifier index: false
    }

    static mapWith = "mongo"
}
