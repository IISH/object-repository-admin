package org.objectrepository.instruction

import org.objectrepository.util.OrUtil

/**
 * WorkflowActiveService
 *
 * Looks at each of the instruction and Stagingfile current status
 * For each status, we check if we can proceed with the next phase in the workflow.
 * The methods we call are conventional, based on a combo [Task][StatusCode] name as defined in Config.workflow.
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
            instruction.id = it._id
            progress(instruction)
        }
    }

    private progress(Instruction instruction) {

        log.info id(instruction) + "Checking for task updates."
        if (instruction.task) {

            if (instruction.ingest == 'pending') {
                instruction.cacheTask = [name: instruction.task.name, statusCode: instruction.task.statusCode]
                try {
                    runMethod(instruction)
                } catch (Exception e) {
                    exception(instruction, e)
                }
            }
            else
            if (instruction.ingest == 'working') {
                mongo.getDB('sa').stagingfile.find(
                        $and: [[fileSet: instruction.fileSet],
                                [$or: [
                                        ['task.statusCode': [$lt: 300]],
                                        ['task.statusCode': 500],
                                        ['task.statusCode': 800]
                                ]]
                        ]
                ).each {
                    Stagingfile stagingfile = it as Stagingfile
                    stagingfile.id = it._id
                    stagingfile.parent = instruction
                    stagingfile.cacheTask = [name: stagingfile.task.name, statusCode: stagingfile.task.statusCode]
                    try {
                        runMethod(stagingfile)
                    } catch (Exception e) {
                        exception(stagingfile, e)
                    }
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
        if (!taskValidationService.hasFSFiles(document)) {
            // We have even become a bigger lie !  We cant ask for an instruction, and be without files. Rules of the game.
            first(document)
        } else {
            def instruction = taskValidationService.hasFSInstruction(document)
            if (instruction) {
                OrUtil.putAll(workflow, document, instruction)
                changeWorkflow('InstructionUpload', document)
            }
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
            if (OrUtil.emptyList(document.workflow)) document.workflow = document.parent.workflow
            document.workflow.each {
                it.statusCode = 0
                it.processed = 0
                it.total = document.task.total
            }
            last(document)
        }
        else {
            next(document)
        }
    }

/**
 * addUpdateUpsertOrDelete
 *
 * The action indicator determines the flow of the file.
 * Where:
 * action=delete : we move to the delete queue
 * action=add : we go to the next phase
 * action=update : with file location, we proceed like add.
 * action=upsert : no location. A simple metadata update
 *
 * Sets the expected tasks.
 *
 * @param document
 * @return
 */
    def Start100(def document) {

        switch (document.action) {
            case 'delete':
                changeWorkflow('FileRemove', document)
                break
            case 'add':
            case 'update':
            case 'upsert':
            default:
                document.workflow = [document.task]
                document.failed = []
                def workflow = (document.parent.workflow.size() == 0) ? document.parent.parent.workflow : document.parent.workflow
                workflow.each {
                    it.statusCode = 0
                    it.processed = 0
                    document.workflow << it
                }
                next(document) // we just go through the mill here. Atomic updates for access should go via the controller
                break
        }
    }

    def FileRemove400(def document) {
        gridFSService.delete(document.pid)
        next(document)
    }

/**
 * Stagingfile800
 *
 * Detect the next task. If not found, we move to the default end of the Road.
 *
 * @param document
 * @return
 */
    void Stagingfile800(document) {

        OrUtil.removeFirst(document.workflow)
        def task = OrUtil.takeFirst(document.workflow) ?: [name: 'EndOfTheRoad']
        log.info id(document) + "Stagingfile800 sets changeWorkflow from " + document.task.name + " to " + task.name
        changeWorkflow(task.name, document)
    }

}
