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
     * Using a first-in-first-out principle.
     */
    void job() {
        mongo.getDB('sa').instruction.find().each {
            def instruction = it as Instruction
            progress(instruction)
        }
    }

    private progress(Instruction instruction) {

        log.info id(instruction) + "Checking for task updates."
        if (instruction.task) {

            if (instruction.ingesting) {
                mongo.getDB('sa').stagingfile.find(
                        $and: [[fileSet: instruction.fileSet],
                                [$or: [
                                        [workflow: [$elemMatch: [n: 0, statusCode: 500]]],
                                        [workflow: [$elemMatch: [n: 0, statusCode: 800]]],
                                        [workflow: [$elemMatch: [n: 0, statusCode: [$lt: 300]]]]
                                ]]
                        ]
                ).each {
                    Stagingfile stagingfile = it as Stagingfile
                    stagingfile.parent = instruction
                    stagingfile.cacheTask = [name: stagingfile.task.name, statusCode: stagingfile.task.statusCode]
                    try {
                        runMethod(stagingfile)
                    } catch (Exception e) {
                        exception(stagingfile, e)
                    }
                }
            } else {
                instruction.cacheTask = [name: instruction.task.name, statusCode: instruction.task.statusCode]
                try {
                    runMethod(instruction)
                } catch (Exception e) {
                    exception(instruction, e)
                }
            }
            if (instruction.change) { save(instruction) }
        }
    }

/**
 * UploadFiles
 *
 * We have an empty fileSet ?
 * The moment we see files, we can proceed offering services.
 * Yet if the folder disappears, we can remove our document.
 *
 * @param document
 * @return
 */
    def UploadFiles(def document) {
        log.info id(document) + "See if we have any files here"
        if (taskValidationService.hasFSFiles(document)) {
            log.info id(document) + "Files seen in fileset"
            last(document)
        }
    }

/**
 * We have files and possibly an xml document processing instruction.
 * If the files are removed, we reset the task.
 * When an instruction is seen (upload), we can proceed reading it in.
 *
 * @param document
 * @return
 */
    def UploadFiles800(Instruction document) {
        if (taskValidationService.hasFSFiles(document)) {
            def instruction = taskValidationService.hasFSInstruction(document)
            if (instruction) {
                OrUtil.putAll(plans, instruction)
                mongo.getDB('sa').instruction.update([_id: document.id],
                        [$set: instruction], false, false
                )
                changeWorkflow('InstructionUpload', document)
            }
        } else {
            // We have even become a bigger lie !  We cant ask for an instruction, and be without files. Rules of the game.
            first(document)
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
        }
        else {
            next(document)
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