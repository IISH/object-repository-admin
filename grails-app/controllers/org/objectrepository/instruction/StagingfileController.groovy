package org.objectrepository.instruction

import org.objectrepository.security.Policy
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
class StagingfileController {

    def springSecurityService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        forward(action: "list", params: params)
    }

    def list = {
        params.view = "list"
        forward(action: "listremote", params: params)
    }

    def listremote = {
        def instructionInstance = Instruction.get(params.orid) // This is the Instruction id
        if (!instructionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'Stagingfile.label', default: 'Instruction'), params.orId])}"
            redirect(controller: "instruction", action: "list")
            return
        }

        if (!springSecurityService.hasValidNa(instructionInstance.na)) {
            render status: 403
            return;
        }

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def stagingfileInstanceList
        def elemMatch = (params.filter_name) ? [name: params.filter_name] : [:]
        switch (params.filter_status) {
            case 'complete':
                elemMatch << [statusCode: [$gt: 799]]
                break
            case 'waiting':
                elemMatch << [statusCode: [$lt: 400]]
                break
            case 'running':
                elemMatch << [statusCode: [$gt: 399, $lt: 700]]
                break
            case 'failure':
                elemMatch << [statusCode: [$gt: 699, $lt: 800]]
                break
        }
        if (elemMatch.size() == 0) {
            stagingfileInstanceList = Stagingfile.findAllByFileSet(instructionInstance.fileSet, params)
        } else {
            def query = [fileSet: instructionInstance.fileSet, workflow: [$elemMatch: elemMatch]]
            stagingfileInstanceList = Stagingfile.collection.find(query).collect() {
                it as Stagingfile
            }
        }

        if (params.view) {
            render(view: params.view, model: [stagingfileInstanceList: stagingfileInstanceList, stagingfileInstanceTotal: Stagingfile.countByFileSet(instructionInstance.fileSet), instructionInstance: instructionInstance])
            params.remove('view')
        }
        else
            [stagingfileInstanceList: stagingfileInstanceList, stagingfileInstanceTotal: Stagingfile.countByFileSet(instructionInstance.fileSet), instructionInstance: instructionInstance]
    }

    def show = {
        params.view = "show"
        redirect(action: "showremote", params: params)
    }

    def showremote = {
        def stagingfileInstance = Stagingfile.get(params.id)
        if (!stagingfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
            forward(action: "list")
            return
        }
        if (!springSecurityService.hasValidNa(stagingfileInstance.parent.na)) { // Make sure we do not see anothers na here...
            response 403
            forward(action: "list")
        }

        if (params.view) {
            render(view: params.view, model: [stagingfileInstance: stagingfileInstance])
        } else {
            render(view: "_showremote", model: [stagingfileInstance: stagingfileInstance])
        }
    }

    def edit = {
        def stagingfileInstance = Stagingfile.get(params.id)
        if (!stagingfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
            forward(action: "list")
            return
        }
        if (!springSecurityService.hasValidNa(stagingfileInstance.parent.na)) { // Make sure we do not see anothers na here...
            response 403
            forward(action: "list")
        }
        def policyInstanceList = Policy.findAllByNa(stagingfileInstance.parent.na)
        [stagingfileInstance: stagingfileInstance, policyList: policyInstanceList.access]
    }

    def update = {
        def stagingfileInstance = Stagingfile.get(params.id)
        if (!springSecurityService.hasValidNa(stagingfileInstance.na)) {
            response 401
            forward(action: "list")
        }

        if (stagingfileInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (stagingfileInstance.version > version) {

                    stagingfileInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'instruction.label', default: 'Instruction')] as Object[], "Another user has updated this Instruction while you were editing")
                    render(view: "edit", model: [stagingfileInstance: stagingfileInstance])
                    return
                }
            }
            stagingfileInstance.properties = params
            if (!stagingfileInstance.hasErrors() && stagingfileInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'stagingfile', default: 'Stagingfile'), stagingfileInstance.id])}"
                redirect(action: "show", id: stagingfileInstance.id)
            }
            else {
                render(view: "edit", model: [stagingfileInstance: stagingfileInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'stagingfile.label', default: 'Stagingfile'), params.id])}"
            redirect(action: "list")
        }
    }
}
