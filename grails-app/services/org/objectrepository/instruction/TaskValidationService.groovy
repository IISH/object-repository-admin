package org.objectrepository.instruction

import org.objectrepository.util.OrUtil

import java.util.regex.Pattern

/**
 * TaskValidationService
 *
 * Main purpose is to validate tasks and confirm status.
 * Each task would require a different kind of validation.
 *
 * @author: Lucien van Wouw <lwo@iisg.nl>
 */
class TaskValidationService {

    static transactional = 'mongo'

    def grailsApplication

    TaskValidationService() {

        /**
         * getServices
         *
         * Each Workflow has a service that invokes it. Hence we also have a controller to invoke the service.
         * Services become available when we have an ingest=pending state.
         *
         * The services is locked when a workflow is running: statusCode < 700 ( can be set via PlanConfig )
         * In all other cases the services is free to be called at a default instruction ingest status of 'pending'.
         * Each service can be filtered in PlanConfig in the service: [ingest:[] ] setting
         *
         * These services are infused in the Instruction class, where other services, controllers and views can
         * access via the getServices property.
         *
         */
        Instruction.metaClass.getServices = {
            _services = (_services) ?: grailsApplication.config.plans.inject([]) {acc, v ->
                def service = v.value.service
                if (service) {
                    def method = service.method
                    def statusCode = (service.statusCode) ?: 700
                    if (task.statusCode >= statusCode && !ingesting) {
                        //noinspection GroovyAssignabilityCheck
                        if (!method || "$method"(delegate)) {
                            def name = (service.name) ?: v.key
                            def controllerAction = Pattern.compile("([A-Z])").matcher(name).replaceAll(" \$1").trim().toLowerCase().split("\\s", 2)
                            def controller = (service.controller) ?: controllerAction[0]
                            def action = (service.action) ?: controllerAction[1]
                            acc << [name: name, controller: controller, action: action]
                        }
                    }
                }
                acc
            }
        }

        Instruction.metaClass.getCountInvalidFiles = {
            countInvalidFiles(delegate)
        }

        Stagingfile.metaClass.getServices = { [] }
        Stagingfile.metaClass.getCountInvalidFiles = { 0 }
    }

    boolean hasChange(def currentDocument, def document) {
        boolean change = (currentDocument.task.statusCode != document.task.statusCode || currentDocument.task.name != document.task.name)
        document.change = (document.change || change)
        change
    }

    boolean hasFileSet(def document) {
        new File(document.fileSet as String).exists()
    }

    boolean hasDBInstruction(def document) {
        Stagingfile.countByFileSet(document.fileSet) != 0
    }

    File FSInstruction(def document) {
        new File(document.fileSet as String, "instruction.xml")
    }

    /**
     * hasFSInstruction
     *
     * See if an instruction is on the fs ( status 200 ) or in the database ( status 800 )
     * The instruction ought to be valid XML and end with a closing or element
     *
     * @param document
     * @return
     */
    def hasFSInstruction(def document) {

        File file = FSInstruction(document)
        if (!file.exists()) return null
        OrUtil.hasFSInstruction(file)
    }

    /**
     * hasValidDBInstruction
     *
     * True is the all files in the FileSet are valid. That is: there are no tasks
     * All to-be-processed and invalid files have a task with statusCode.
     *
     * @return
     */
    protected boolean hasValidDBInstruction(document) {
        hasDBInstruction(document) && countInvalidFiles(document) == 0
    }

    protected int countInvalidFiles(def document) {
        document.findFilesWithCursorByQuery("{'workflow.name':'InstructionValidate','workflow.n':0}").count()
    }

    /**
     * Recurs, until we find our first file
     * @param dir
     * @return
     */
    private boolean hasFSFiles(File dir) {

        for (File file : dir.listFiles()) {
            if (file.name[0].equals(".")) continue;
            if (file.isFile()) return true;
            return hasFSFiles(file)
        }
        false;
    }

    /**
     * Checks if there is at least on file in the staging area fileset
     *
     * @param document
     * @return
     */
    boolean hasFSFiles(def document) {
        return hasFSFiles(new File(document.fileSet as String))
    }
}
