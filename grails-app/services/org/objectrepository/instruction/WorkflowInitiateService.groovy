package org.objectrepository.instruction

import org.objectrepository.util.Normalizers
import org.objectrepository.util.OrUtil

/**
 * WorkflowInitiateService
 *
 * Kick starts the workflow sessions for OR instructions. A new workflow is instantiated the moment
 * a fileSet is introduced into the staging area.
 *
 * Decommissions instructions that have completed there work.
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 */
class WorkflowInitiateService extends WorkflowJob {

    /**
     * For each new root folder
     * which not has a fileset declared
     * we add an empty instruction in the database.
     *
     * @return
     */
    void job() {
        cpAdminFolders(Profile.list().collect {it.na})
    }

    private def cpAdminFolders(def nas) {
        for (String na : nas) {

            log.info "Checking for new fileSets for " + na
            addInstruction(na)

            log.info "Checking for instructions that have no fileset for " + na
            uploadInstruction(na)

            log.info "Decommissions instructions that have done their job for " + na
            decommissionInstruction(na)
        }
    }

    /**
     * Go over each folder and see if a workflow needs to be added.
     * @param folder
     * @param na
     * @return
     */
    private def addInstruction(String na) {

        File folder = new File(home, na) // CP_ADMIN directory
        log.info "Looking in " + folder.absolutePath
        for (File dir : folder.listFiles()) {               // for each CP_USER home directory
            if (!dir.name[0].equals(".")) {
                for (File file : dir.listFiles()) {  // for each fileset folders....
                    if (file.isDirectory() && !file.name[0].equals(".")) { // Ignore hidden folders... hence the .
                        addInstruction(na, file)
                    }
                }
            }
        }
    }

    /**
     * uploadInstruction
     *
     * Checks if an instruction is present or not.
     * Remove an instruction when there is no associate fileSet
     *
     * @param na
     */
    private void uploadInstruction(String na) {
        Instruction.findAllByNa(na)?.each {
            if (it.fileSet[0] != '.' && !taskValidationService.hasFileSet(it)) {
                it.delete() // We have become a lie... no fileset at all.
            } else if (it.task.name == 'UploadFiles') {
                runMethod(it)
            }
        }
    }

    /**
     * decommissionInstruction
     *
     * Sets the statusCode of the instruction to 900 should all task be completed.
     * Completed without issues: all stagingfiles have a statusCode of 800 or higher
     * ToDo: make the check with mapReduce so we know the failure count in one go.
     *
     * @param _na
     */
    private void decommissionInstruction(String _na) {

        //noinspection GroovyAssignabilityCheck
        mongo.getDB('sa').instruction.find(
                $and: [
                        [na: _na],
                        [workflow: [$elemMatch: [name: 'InstructionIngest', statusCode: 800]]]
                ]
        ).each {
            Instruction instructionInstance = it as Instruction
            int countStagingfiles = Stagingfile.countByFileSet(instructionInstance.fileSet)
            int count = mongo.getDB('sa').stagingfile.count([fileSet: instructionInstance.fileSet, workflow: [$elemMatch:
                    [name: 'EndOfTheRoad', statusCode: [$gt: 699]]]]) // statusCode ought to be 850 (problems) or 900
            if (countStagingfiles == count) {
                log.info id(instructionInstance) + "Decomissioning (Instruction is done)"
                instructionInstance.task.statusCode = 900
                if (count == 0) {
                    // ToDo: sent message notification
                    if (instructionInstance.deleteCompletedInstruction) {
                        delete(instructionInstance)
                        return
                    }
                } else {
                    count = mongo.getDB('sa').stagingfile.count([fileSet: instructionInstance.fileSet, workflow: [$elemMatch: [name: 'EndOfTheRoad', statusCode: 900]]])
                    instructionInstance.task.info = (count == countStagingfiles) ? "Completed" : "Done, but with some unresolved issues"
                    log.info "Retry instruction: no"
                    //retry(instructionInstance)
                }
                save(instructionInstance)
            }
        }
    }

/**
 * Checks for a workflow..... if not available, create one with defaults.
 *
 * @param na
 * @param entry
 * @return
 */
    private void addInstruction(def na, File entry) {

        final String fileSet = Normalizers.normalize(entry)
        def instructionInstance = Instruction.findByFileSet(fileSet)
        if (instructionInstance) return

        log.info "Fileset found, but no declaration in database. Creation declaration for " + entry
        instructionInstance = new Instruction(na: na, fileSet: fileSet)
        instructionInstance.change = true

        log.info "FileSet found: " + fileSet
        //noinspection GroovyAssignabilityCheck
        instructionInstance.task = [name: 'UploadFiles', statusCode: 0]
        instructionInstance.task.taskKey()
        try {
            if (instructionInstance.save(flush: true)) {
                sendMessage("activemq:status", instructionInstance.task.identifier)
            } else {
                instructionInstance.errors.each {
                    log.error "Errors " + it
                }
            }
        } catch (Exception e) {
            exception(instructionInstance, e)
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
}
