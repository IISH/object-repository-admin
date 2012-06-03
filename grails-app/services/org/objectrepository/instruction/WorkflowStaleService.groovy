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

    def mongo
    private static int max = 100
    private static int period = 180000 // three minutes of no response from the message queue client. And then the task becomes stale.
    private static final int StatusCodeTaskReceipt = 400;
    private static final int StatusCodeTaskComplete = 500;

    /**
     * job
     *
     * Find all documents with a stale task.
     * Sets the task statusCode to 500 is found stale.
     * This only means the active workflow will pick it up and move it to a 600 check
     */
    void job() {

        final date = new Date(new Date().time - period) // past tense
        ['instruction', 'stagingfile'].each {
            mongo.getDB('sa')."$it".update(
                    workflow: [$elemMatch: [n: 0, statusCode: StatusCodeTaskReceipt, end: [$lt: date]]],
                    [$set: ['workflow.$.statusCode': StatusCodeTaskComplete, 'workflow.$.exitValue': Integer.MAX_VALUE]],
                    false,
                    false
            )
        }
    }
}
