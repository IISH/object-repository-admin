package org.objectrepository.files

import grails.plugins.springsecurity.Secured
import org.objectrepository.instruction.Instruction
import org.objectrepository.security.Policy
import org.objectrepository.util.OrUtil

@Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
class OrfileController {

    def springSecurityService
    def workflowActiveService
    def gridFSService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        forward(action: "list", params: params)
    }

    def list() {

        if ( params.pid ) return findbypid()

        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.offset = params.offset ? params.int('offset') : 0
        params.order = !params.order || params.order == "asc" ? 1 : -1
        params.sort = params.sort ?: "_id"
        final String na = (springSecurityService.hasRole('ROLE_ADMIN')) ? params.na : springSecurityService.principal.na
        final labels = gridFSService.labels(na).plus(0, 'select label')
        if (params.label == null) return [orfileInstanceListTotal: 0, labels: labels]
        def orfileInstanceList = gridFSService.findAllByLabel(na, params)
        [orfileInstanceList: orfileInstanceList, orfileInstanceListTotal: gridFSService
                .countByNa(na, params),
                labels: labels]
    }

    def findbypid() {
        def orfileInstanceList = []
        final String na = (springSecurityService.hasRole('ROLE_ADMIN')) ? params.na : springSecurityService.principal.na
        if (params.pid) {
            def file = gridFSService.get(na, params.pid)
            if (file) orfileInstanceList << file
        }
        final labels = gridFSService.labels(na).plus(0, 'select label')
        [orfileInstanceList: orfileInstanceList, orfileInstanceListTotal: orfileInstanceList.size(), labels: labels]
    }

    def show() {
        final String na = (springSecurityService.hasRole('ROLE_ADMIN')) ? params.na : springSecurityService.principal.na
        def orfileInstance = gridFSService.get(na, new String(params.id.decodeBase64()))
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), new String(params.id.decodeBase64())])
            redirect(action: "list")
            return
        }

        if (!springSecurityService.hasValidNa(na)) {
            response 401
            forward(action: "list")
        }

        [orfileInstance: orfileInstance]
    }

    def edit() {
        final String na = (springSecurityService.hasRole('ROLE_ADMIN')) ? params.na : springSecurityService.principal.na
        def orfileInstance = gridFSService.get(na, new String(params.id.decodeBase64()))
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), new String(params.id.decodeBase64())])
            forward(action: "list")
            return
        }

        if (!springSecurityService.hasValidNa(na)) {
            response 401
            forward(action: "list")
        }

        def policyList = Policy.findAllByNa(na)
        [orfileInstance: orfileInstance, policyList: policyList]
    }

    def update() {
        final String na = (springSecurityService.hasRole('ROLE_ADMIN')) ? params.na : springSecurityService.principal.na
        def orfileInstance = gridFSService.get(na, new String(params.id.decodeBase64()))
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), new String(params.id.decodeBase64())])
            redirect(action: "list")
            return
        }

        if (!springSecurityService.hasValidNa(na)) {
            response 401
            forward(action: "list")
        }

        orfileInstance.master.metadata.access = params.access
        orfileInstance.master.metadata.label = params.label

        gridFSService.update(orfileInstance.master)

        flash.message = message(code: 'default.updated.message', args: [message(code: 'files.label', default: 'Files'), params.id])
        redirect(action: "show", id: params.id)
    }

    /**
     * recreate
     *
     * An instruction will be re created based on the stored files.
     *
     * @return
     */
    def recreate() {
        final String na = (springSecurityService.hasRole('ROLE_ADMIN')) ? params.na : springSecurityService.principal.na

        def file = gridFSService.findByField(na, 'master', 'metadata.label', params.label)
        if (!file?.metaData?.fileSet) {
            flash.message = 'No such label: ' + params.label
            redirect(action: "list")
            return
        }

        Instruction instructionInstance = Instruction.findByFileSet(file.metaData.fileSet)
        if (instructionInstance) {
            if (instructionInstance.task?.total != 0) {
                flash.message = "There is already an instruction for this dataset: " + file.metaData.fileSet
                redirect(action: "list")
                return
            }
        } else
            instructionInstance = new Instruction()

        instructionInstance.na = na
        instructionInstance.fileSet = file.metaData.fileSet
        instructionInstance.autoIngestValidInstruction = false
        instructionInstance.label = params.label
        instructionInstance.task = [name: OrUtil.camelCase(['Instruction', actionName])]
        instructionInstance.task.taskKey()

        workflowActiveService.first(instructionInstance)
        if (instructionInstance.save(flush: true)) {
            try {
                sendMessage("activemq:status", instructionInstance.task.identifier)
            }
            catch (Exception e) {
                // message queue may be down
                log.warn e.message
                flash.message = e.message
                redirect(action: "list")
                return
            }
        }
        redirect(controller: 'instruction', action: 'show', id: instructionInstance.id)
    }

    def download() {

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/xml")
        response.addHeader('Content-disposition', 'attachment; filename="orfiles.xml"')
        params.pid = (params.pid) ? new String(params.pid.decodeBase64()) : null
        gridFSService.writeOrfiles(params, springSecurityService.principal.na, response.outputStream)
    }
}
