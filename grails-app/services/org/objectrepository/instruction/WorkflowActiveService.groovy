package org.objectrepository.instruction

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
     * Incoming message from the messagequeue
     *
     * @param identifier
     */
    public void status(String identifier) {

        if (identifier) {
            def query = [workflow: [$elemMatch: [n: 0, identifier: identifier]]]
            def document = mongo.getDB('sa').instruction.findOne(query) as Instruction
            if (document) {
                log.info id(document) + "Status from message queue for instruction"
                progress(document)
            } else {
                document = mongo.getDB('sa').stagingfile.findOne(query) as Stagingfile
                if (document) {
                    log.info id(document) + "Status from message queue for stagingfile"
                    stagingfile(document)
                }
            }
        }
    }

    /**
     * dlq
     *
     * The DLQ will resend the message again and update the task date. No need to produce a new task.
     *
     * @param msg
     */
    public void dlq(String msg) {
        final document = new XmlParser().parseText(msg)
        final String queue = document.workflow.name.text()
        final String collectionName = OrUtil.splitCamelcase(queue)[0].toLowerCase()
        final String identifier = document.workflow.identifier.text()
        final collection = mongo.getDB('sa').getCollection(collectionName)
        def tmp = collection.findAndModify([workflow: [$elemMatch: [n: 0, name: queue, statusCode: 300, identifier: identifier]]], [$set:['workflow.$.end': new Date()]])
        if ( tmp ) sendMessage(["activemq", queue].join(":") + "?timeToLive=" + timeToLive(), msg)
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
        }
        else {
            next(document)
        }
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
     * For each custom derivative we can disable the production of a derivative if it was already ingested as a custome file
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