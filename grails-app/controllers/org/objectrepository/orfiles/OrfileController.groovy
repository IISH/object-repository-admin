package org.objectrepository.orfiles

import grails.plugins.springsecurity.Secured
import org.objectrepository.instruction.Instruction
import org.objectrepository.security.Policy
import org.objectrepository.util.OrUtil

import org.objectrepository.security.NamingAuthorityInterceptor
import org.objectrepository.instruction.Stagingfile

@Secured(['IS_AUTHENTICATED_FULLY'])
class OrfileController extends NamingAuthorityInterceptor {

    def springSecurityService
    def workflowActiveService
    def gridFSService
    def downloadService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        forward(action: "list", params: params)
    }

    def list() {

        if (params.pid) return findbypid()

        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.offset = params.offset ? params.int('offset') : 0
        params.order = !params.order || params.order == "asc" ? 1 : -1
        params.sort = params.sort ?: "_id"
        final labels = gridFSService.labels(params.na).plus(0, 'select label')
        if (params.label == null) return [orfileInstanceListTotal: 0, labels: labels]
        def orfileInstanceList = gridFSService.findAllByLabel(params.na, params)
        [orfileInstanceList: orfileInstanceList, orfileInstanceListTotal: gridFSService
                .countByNa(params.na, params),
                labels: labels]
    }

    def findbypid() {
        def orfileInstanceList = []
        if (params.pid) {
            def file = gridFSService.get(params.na, params.pid)
            if (file) orfileInstanceList << file
        }
        final labels = gridFSService.labels(params.na).plus(0, 'select label')
        [orfileInstanceList: orfileInstanceList, orfileInstanceListTotal: orfileInstanceList.size(), labels: labels]
    }

    def show() {
        def orfileInstance = gridFSService.get(params.na, new String(params.id.decodeBase64()))
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), new String(params.id.decodeBase64())])
            //redirect uri: createLink( base: '/' + params.na, controller: controllerName, action: "list")
            forward(action: 'list')
        } else
            [orfileInstance: orfileInstance]
    }

    def edit() {
        def orfileInstance = gridFSService.get(params.na, new String(params.id.decodeBase64()))
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), new String(params.id.decodeBase64())])
            forward(action: "list")
        }
        else
            [orfileInstance: orfileInstance, policyList: Policy.findAllByNa(params.na)]
    }

    def update() {
        def orfileInstance = gridFSService.get(params.na, new String(params.id.decodeBase64()))
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), new String(params.id.decodeBase64())])
            //redirect uri: createLink( base: '/' + params.na, controller: controllerName, action: "list")
            forward(action: 'list')
        } else {
            orfileInstance.master.metadata.access = params.access
            orfileInstance.master.metadata.label = params.label
            orfileInstance.master.metadata.objid = params.objid
            orfileInstance.master.metadata.seq = params.seq

            gridFSService.update(orfileInstance.master)

            flash.message = message(code: 'default.updated.message', args: [message(code: 'files.label', default: 'Files'), params.id])
            //redirect uri: createLink( base: '/' + params.na, controller: controllerName, action: "show", id:params.id)
            forward(action: 'show', id: params.id)
        }
    }

    /**
     * recreate
     *
     * An instruction will be re created based on the stored files's shared label value.
     *
     * @return
     */
    def recreate() {

        def file = gridFSService.findByField(params.na, 'master', 'metadata.label', params.label)
        if (!file?.metaData?.fileSet) {
            flash.message = 'No such label: ' + params.label
            // redirect uri: createLink( base: '/' + params.na, controller: controllerName, action: "list")
            forward(action: 'list')
            return
        }

        Instruction instructionInstance = Instruction.findByFileSet(file.metaData.fileSet)
        if (instructionInstance) {
            if (Stagingfile.countByFileSet(file.metaData.fileSet) != 0) {
                flash.message = "There is already an instruction for this dataset: " + file.metaData.fileSet
                redirect(action: "list")
                return
            }
        } else
            instructionInstance = new Instruction()

        instructionInstance.na = params.na
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
        //redirect uri: createLink( base: '/' + params.na, controller: "instruction", action: "show", id: instructionInstance.id)
        forward(action: 'show', id: instructionInstance.id)
    }

    def download() {

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/xml")
        params.pid = (params.pid) ? new String(params.pid.decodeBase64()) : null
        downloadService.writeOrfiles(params, response.outputStream)
    }
}
