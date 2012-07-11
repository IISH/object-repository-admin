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
 * Possible the service node has aborted a job and was not able to tell us about it. Or it did succeed.
 * Another rare possibility is that the client is down or even the message got lost.
 *
 * In that case, the move the statusCode of the job to 600 and will await validation.
 *
 * author: Lucien van Wouw <lwo@iisg.nl>
 */
class WorkflowStaleService extends WorkflowJob {

    private static final int StatusCodeQueued = 300;
    private static final int StatusCodeRestartQueued = 100;
    private static int periodQueued = 7200000 // Two hours of queueing status. And then the task becomes stale.
    private static final int StatusCodeTaskReceipt = 400;
    private static final int StatusCodeTaskCompleteReceipt = 500;
    private static int periodTaskReceipt = 180000 // three minutes of no response from the message queue client. And then the task becomes stale.

    /**
     * job
     *
     * Find all documents with a stale task.
     * Sets the task statusCode 300 to StatusCodeStart if found stale.
     * Sets the task statusCode 400 to StatusCodeTaskCompleteReceipt if found stale.
     * This only means the active workflow will pick it up and move it to a 600 check where it will probably retry it.
     */
    void job() {

        final long time = new Date().time
        checkStaleness(StatusCodeQueued, StatusCodeRestartQueued, new Date(time - periodQueued))
        checkStaleness(StatusCodeTaskReceipt, StatusCodeTaskCompleteReceipt, new Date(time - periodTaskReceipt))
        //checkStaleness()
    }

    private void checkStaleness(int currentStatusCode, int newStatusCode, Date date) {
        ['instruction', 'stagingfile'].each {
            mongo.getDB('sa')."$it".update(
                    workflow: [$elemMatch: [n: 0, statusCode: currentStatusCode, end: [$lt: date]]],
                    [$set: ['workflow.$.statusCode': newStatusCode, 'workflow.$.exitValue': Integer.MAX_VALUE, 'workflow.$.identifier': null]],
                    false,
                    false
            )
        }
    }

    /**
     * checkStaleness
     *
     * The shardprefix collection is used to offer slots to the clients that perform put operations.
     * Should we find a task identifier then we encountered a stale lock, which we remove here.
     */
    /*private void checkStaleness() {
        mongo.getDB('sa').'shardprefix'.find([identifier: [$exists: true]]).each {
            def task = mongo.getDB('sa').'stagingfile'.findOne(['workflow.identifier': it.identifier], [_id:1])
            if (!task) {
                mongo.getDB('sa').'shardprefix'.update(
                        _id: it._id,
                        [$unset: [identifier: 1, hostname: 1]],
                        false,
                        true
                )
            }
        }
    }*/
}
