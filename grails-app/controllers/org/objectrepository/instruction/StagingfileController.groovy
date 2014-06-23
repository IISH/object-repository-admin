package org.objectrepository.instruction

import org.objectrepository.security.InterceptorValidation
import org.objectrepository.security.Policy
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class StagingfileController extends InterceptorValidation {

    def springSecurityService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        forward(action: "list", params: params)
    }

    def list() {
        params.view = "list"
        forward(action: "listremote", params: params)
    }

    def listremote() {
        def instructionInstance = Instruction.get(params.orid) // This is the Instruction id
        if (!instructionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'Stagingfile.label', default: 'Instruction'), params.orId])}"
            forward(controller: "instruction", action: "list")
            return
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
                elemMatch << [$or: [[statusCode: [$gt: 699, $lt: 800]], [statusCode: 850]]]
                break
        }

        int count
        if (elemMatch.size() == 0) {
            stagingfileInstanceList = Stagingfile.findAllByFileSet(instructionInstance.fileSet, params)
            count = Stagingfile.countByFileSet(instructionInstance.fileSet)
        } else {
            def query = [fileSet: instructionInstance.fileSet, workflow: [$elemMatch: elemMatch]]
            stagingfileInstanceList = Stagingfile.collection.find(query).collect() {
                it as Stagingfile
            }
            count = Stagingfile.collection.count(query)
        }

        if (params.view) {
            render(view: params.view, model: [stagingfileInstanceList: stagingfileInstanceList,
                    stagingfileInstanceTotal: count,
                    instructionInstance: instructionInstance])
            params.remove('view')
        } else
            [stagingfileInstanceList: stagingfileInstanceList, stagingfileInstanceTotal: count,
                    instructionInstance: instructionInstance]
    }

    def show() {
        params.view = "show"
        forward(action: "showremote", params: params)
    }

    def showremote() {
        def stagingfileInstance = Stagingfile.get(params.id)
        if (!stagingfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
            forward(action: "list")
            return
        }
        if (params.view) {
            render(view: params.view, model: [stagingfileInstance: stagingfileInstance])
        } else {
            render(view: "_showremote", model: [stagingfileInstance: stagingfileInstance])
        }
    }

    def edit(Stagingfile stagingfileInstance) {

        switch (status(stagingfileInstance)) {
            case HttpStatus.OK:
                respond stagingfileInstance, model: [stagingfileInstance: stagingfileInstance, policyList: Policy.findAllByNa(stagingfileInstance.parent.na).access]
                break
            case HttpStatus.BAD_REQUEST:
                respond(stagingfileInstance, view: 'show', model: [stagingfileInstance: stagingfileInstance, policyList: Policy.findAllByNa(stagingfileInstance.parent.na).access])
                break
        }
    }

    def update(Stagingfile stagingfileInstance) {

        switch (status(stagingfileInstance)) {
            case HttpStatus.OK:
                respond(stagingfileInstance, view: 'show', model: [stagingfileInstance: stagingfileInstance])
                break
            case HttpStatus.BAD_REQUEST:
                respond(stagingfileInstance, view: 'edit', model: [stagingfileInstance: stagingfileInstance])
                break
        }
    }
}
