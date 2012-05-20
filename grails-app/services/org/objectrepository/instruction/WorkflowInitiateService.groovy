package org.objectrepository.instruction

import org.objectrepository.util.Normalizers

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

            log.info "Checking for new fileSets."
            addInstruction(na)

            log.info "Checking for instructions that have no fileset."
            removeInstruction(na)

            log.info "Decommissions instructions that have done their job"
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
     * removeInstruction
     *
     * Remove an instruction when there is no associate fileSet
     *
     * @param na
     */
    private void removeInstruction(String na) {
        Instruction.findAllByNa(na)?.each {
            if (!taskValidationService.hasFileSet(it)) {
                it.delete() // We have become a lie... no fileset at all.
            }
        }
    }

    private void decommissionInstruction(String _na) {

        def instructionList = Instruction.where({
            na == _na && task.name == 'InstructionIngest' &&
                    task.statusCode == 800
        }).list()
        for (Instruction instructionInstance : instructionList) {
            if (instructionInstance.findFilesWithCursor().count() == 0) {
                log.info id(instructionInstance) + "Decomissioning (InstructionDone)"
                instructionInstance.delete()
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
        instructionInstance.save()
    }
}
