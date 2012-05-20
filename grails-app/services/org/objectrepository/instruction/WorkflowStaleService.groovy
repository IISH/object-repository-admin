package org.objectrepository.instruction

/**
 * WorkflowStaleService
 *
 * Looks at each of the instruction and Stagingfile current status
 *
 * The service node will sent frequent status updated ( or confirmations of it ) to the status queue.
 * As the document.task.end date is then updated, we always know the process is still active.
 *
 * Should however the status not be updated... we have detected a problem.
 * Possible the service node has aborted a job and was not able to tell us about it. Or it did succeed
 *
 * In that case, the move the statusCode of the job to 600 and will await validation.
 *
 * author: Lucien van Wouw <lwo@iisg.nl>
 */
class WorkflowStaleService extends WorkflowJob {

    private static int max = 100
    private static int period = 180000 // three minutes of no response from the message queue client. And then the task becomes stale.
    private static final int StatusCodeTaskReceipt = 400;
    private static final int StatusCodeTaskComplete = 500;

    /**
     * job
     *
     * Find all documents with a stale task
     */
    void job() {

        final date = new Date(new Date().time - period) // past tense
        invalidate(Instruction.where({task.statusCode == StatusCodeTaskReceipt && task.end < date}).list([max: max]))
        invalidate(Stagingfile.where({task.statusCode == StatusCodeTaskReceipt && task.end < date}).list([max: max]))
    }

    /**
     * invalidate
     *
     * Sets the task statusCode to 500
     *
     * @param instanceList
     */
    private void invalidate(def instanceList) {

        instanceList.each {
            println("The task has become stale for " + it.id + ":" + it.task.name)
            it.task.statusCode = StatusCodeTaskComplete
            it.task.exitValue = Integer.MAX_VALUE
            it.save()
        }
    }
}
