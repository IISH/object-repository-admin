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

    int max = 100
    int period = 180000 // three minutes of no activity... and then the task becomes stale.

    /**
     * job
     *
     * Find all documents with a stale task
     */
    void job() {

        final date = new Date(new Date().time - period) // past tense
        invalidate(Instruction.where({task.statusCode == 400 && task.end < date}).list([max: max]))
        invalidate(Stagingfile.where({task.statusCode == 400 && task.end < date}).list([max: max]))
    }

    /**
     * invalidate
     *
     * Sets the task statusCode to 600 to  ( stale )
     *
     * @param instanceList
     */
    private void invalidate(def instanceList) {

        instanceList.each {
            println("The task has become stale for " + it.id + ":" + it.task.name)
            it.task.statusCode = 600
            save(it)
        }
    }
}
