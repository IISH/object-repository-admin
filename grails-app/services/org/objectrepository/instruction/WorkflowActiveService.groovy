package org.objectrepository.instruction

import com.mongodb.DBPortPool
import org.objectrepository.util.OrUtil

/**
 * WorkflowActiveService
 *
 * Looks at each of the instruction and Stagingfile current status
 * For each status, we check if we can proceed with the next phase in the workflow.
 * The methods we call are conventional, based on a combo [Task][StatusCode] name as defined in PlansConfig.plans.
 *
 * author: Lucien van Wouw <lwo@iisg.nl>
 */
class WorkflowActiveService extends WorkflowJob {

    def gridFSService
    private int failTolerance = 10

    /**
     * job
     *
     * Process the documents in the staging area.
     */
    void job() {
        final Date expired = OrUtil.expirationDate(instructionMessageTTL)
        mongo.getDB('sa').instruction.find(['workflow.end': [$lt: expired]]).each {
            def instruction = it as Instruction
            progress(instruction)
        }
    }

    /**
     * progress
     *
     * @param instruction
     * @param check The system communicated via messages. However, messages can get lost. A separate query will check
     * the status of the task if these are older than the check data.
     * @return
     */
    private progress(Instruction instruction, Date expired = OrUtil.expirationDate(messageTTL)) {

        log.info id(instruction) + "Checking for task updates."
        if (instruction.ingesting) {
            mongo.getDB('sa').stagingfile.find(
                    $and: [
                            [fileSet: instruction.fileSet],
                            [workflow: [$elemMatch: [n: 0, statusCode: [$lt: 801], end: [$lt: expired]]]]
                    ]
            ).each {
                it.parent = instruction
                stagingfile(it as Stagingfile)
            }
        } else {
            instruction.cacheTask = [name: instruction.task.name, statusCode: instruction.task.statusCode]
            try {
                runMethod(instruction)
            } catch (Exception e) {
                exception(instruction, e)
            }
        }
        if (instruction.change) save(instruction)
    }

    private void stagingfile(Stagingfile stagingfile) {

        stagingfile.cacheTask = [name: stagingfile.task.name, statusCode: stagingfile.task.statusCode]
        try {
            runMethod(stagingfile)
        } catch (Exception e) {
            exception(stagingfile, e)
        }
    }

    /**
     * status
     *
     * Incoming message from the message queue
     *
     * @param identifier
     */
    public void status(String identifier) {

        if (identifier) {
            def query = [workflow: [$elemMatch: [n: 0, identifier: identifier]]]
            def document = null
            try {
                document = mongo.getDB('sa').instruction.findOne(query) as Instruction
            } catch (DBPortPool.ConnectionWaitTimeOut e) {
                log.error(e)
                log.error("The server instance needs to restart. See for a possible explanation https://jira.mongodb.org/browse/JAVA-767")
                tolerance()
            }
            if (document) {
                log.info id(document) + "Status from message queue for instruction"
                progress(document)
            } else {
                try {
                    document = mongo.getDB('sa').stagingfile.findOne(query) as Stagingfile
                } catch (DBPortPool.ConnectionWaitTimeOut e) {
                    log.error(e)
                    log.error("The server instance may need to restart. See for a possible explanation https://jira.mongodb.org/browse/JAVA-767")
                    tolerance()
                }
                if (document) {
                    log.info id(document) + "Status from message queue for stagingfile"
                    stagingfile(document)
                }
            }
        }
    }

    void tolerance() {
        if (failTolerance--< 0) System.exit(-1)
    }

    /**
     * StagingfileBindObjIds100
     *
     * Only one document with an objid needs to address this queue
     * Here we assume the seq = 1 is enough to proceed. Of course it is possible an instruction may not have a
     * seq=1 or multiple identical values.
     *
     * @param document
     * @return
     */
    def StagingfileBindObjId100(Stagingfile document) {

        if (document.objid && document.seq == 1) {
            next(document)
        } else {
            last(document)
        }
    }



/**
 * InstructionIngest100
 *
 * Determine if this ingest is about packaging or individual file ingest.
 *
 * @param document
 * @return
 */
    def InstructionIngest100(def document) {

        if ('InstructionPackage' in document.plan) {
            changeWorkflow('InstructionPackage', document)
        } else {
            next(document)
        }
    }


/**
 * InstructionIngest600
 *
 * Verify that all stagingfile documents are correctly tasked.
 * We know when all files ought to have a task and that task ought to be Start.
 *
 * @param document
 * @return
 */
    def InstructionIngest600(def document) {

        if (document.task.exitValue == 0) {
            OrUtil.setInstructionPlan(document)
            last(document)
        } else {
            next(document)
        }
    }


    /**
     * InstructionPackage800
     *
     * When we reach this status, the instruction can be removed in its entirety.
     *
     * @param document
     * @return
     */
    def InstructionPackage800(def document) {
        document.delete()
    }


    /**
     * InstructionRecreate600
     *
     * Verify that we have at least one declared file
     *
     * @param document
     */
    def InstructionRecreate600(def document) {
        int total = Stagingfile.countByFileSet(document.fileSet)
        if (total == 0) {
            retry(document)
        } else {
            changeWorkflow('InstructionUpload800', document)
            document.task.total = total
        }
    }

    /**
     * StagingfileIngestCustomLevel1
     * StagingfileIngestCustomLevel2
     * StagingfileIngestCustomLevel3
     *
     * For each custom derivative we can disable the production of a derivative if it was already ingested as a custom file
     * or continue if this wsa not the case.
     *
     * @param document
     * @return
     */
    def StagingfileIngestCustomLevel1600(def document) {
        StagingfileIngestCustomLevel(document, 'StagingfileIngestLevel1')
    }

    def StagingfileIngestCustomLevel2600(def document) {
        StagingfileIngestCustomLevel(document, 'StagingfileIngestLevel2')
    }

    def StagingfileIngestCustomLevel3600(def document) {
        StagingfileIngestCustomLevel(document, 'StagingfileIngestLevel3')
    }

    private void StagingfileIngestCustomLevel(def document, String name) {
        if (document.task.exitValue == 0) {
            document.workflow.find {
                it.name == name
            }?.statusCode = 800
            last(document)
        } else {
            retry(document)
        }
    }

/**
 * Stagingfile800
 *
 * Detect the next task.
 *
 * @param document
 * @return
 */
    void Stagingfile800(document) {

        nextWorkflow(document)
    }
}