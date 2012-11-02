package org.objectrepository.instruction

import org.objectrepository.security.Policy
import org.objectrepository.util.OrUtil
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
class InstructionController {

    def grailsApplication
    def springSecurityService
    def workflowActiveService
    def downloadService

    static allowedMethods = [save: "POST", update: "POST", uploadfile: "POST"]

    def index = {
        forward(action: "list", params: params)
    }

    def list = {
        params.view = "list"
        forward(action: "listremote", params: params)
    }

    def listremote = {
        params.max = Math.min(params.max ? params.int('max') : 5, 10)
        if (!params.sort) params.sort = 'label';

        def instructionInstanceList = (springSecurityService.hasRole('ROLE_ADMIN')) ?
            Instruction.list(params) :
            Instruction.findAllByNa(springSecurityService.principal.na, params)
        int count = (springSecurityService.hasRole('ROLE_ADMIN')) ? Instruction.count() : Instruction.countByNa(springSecurityService.principal.na)

        if (params.view) {
            render(view: params.view, model: [instructionInstanceList: instructionInstanceList, instructionInstanceTotal: count])
            params.remove('view')
        }
        else
            [instructionInstanceList: instructionInstanceList, instructionInstanceTotal: count]
    }

    def show = {
        params.view = "show"
        redirect(action: "showremote", params: params)
    }

    def showremote = {
        def instructionInstance = instructionAvailable()
        if (!instructionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
            redirect(action: "list")
        }
        else if (instructionInstance.status == 200) {
            OrUtil.setInstructionPlan(instructionInstance)
            if (params.view) {
                render(view: params.view, model: [instructionInstance: instructionInstance])
            } else {
                render(view: "_showremote", model: [instructionInstance: instructionInstance])
            }
        }
    }

    def autocreate = {
        runMethod()
    }

    def declare = {
        runMethod()
    }

    def remove = {
        runMethod()
    }

    def ingest = {
        runMethod()
    }

    def retry = {
        runMethod()
    }

    def validate = {
        runMethod()
    }

    def upload = {
        def instructionInstance = serviceAvailable()
        if (!instructionInstance) return

        final File file = new File(instructionInstance.fileSet, "instruction.xml")
        def instruction = (request.method == "POST") ? request.getFile("instruction") : null
        if (instruction && !instruction.empty) {
            instruction.transferTo(file)
            instructionInstance.delete(flush: true)
            redirect(controller: "instruction", action: "list")
        } else if (params.instruction) {
            instructionInstance.delete(flush: true)
            redirect(controller: "instruction", action: "list")
        }
        [instructionInstance: instructionInstance, fileExists: file.exists()]
    }

    def download = {
        // Fetch the instruction, render 404 if not found or it still needs to be
        // validated.
        def instructionInstance = serviceAvailable()
        if (instructionInstance && params.transport) {
            def writer
            if (params.transport == 'http') {
                response.setCharacterEncoding("utf-8");
                response.setContentType("text/xml")
                response.addHeader("content-disposition", "attachment; filename=instruction.xml")
                writer = response.outputStream
            } else {
                final File file = new File(instructionInstance.fileSet, "instruction.xml")
                writer = file.newWriter('UTF-8')
            }
            downloadService.write(writer, instructionInstance)
            redirect(controller: "instruction", action: "list")
        }
        [instructionInstance: instructionInstance]
    }

    def edit = {

        def instructionInstance = Instruction.get(params.id)
        if (!instructionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id]) }"
            redirect(action: "list")
        }
        if (!springSecurityService.hasValidNa(instructionInstance.na)) {
            response 401
            forward(action: "list")
        }
        else if (instructionInstance.status == 200) {
            OrUtil.setInstructionPlan(instructionInstance)
            def policyInstanceList = Policy.findAllByNa(instructionInstance.na)
            [instructionInstance: instructionInstance, policyList: policyInstanceList.access]
        }
    }

    def update = {
        def instructionInstance = Instruction.get(params.id)
        if (!springSecurityService.hasValidNa(instructionInstance.na)) {
            response 401
            forward(action: "list")
        }

        if (instructionInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (instructionInstance.version > version) {

                    instructionInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                            [message(code: 'instruction.label', default: 'Instruction')] as Object[],
                            "Another user has updated this Instruction while you were editing")
                    render(view: "edit", model: [instructionInstance: instructionInstance])
                    return
                }
            }

            instructionInstance.properties = params
            instructionInstance.action = params.action1
            if (instructionInstance.plan) {
                instructionInstance.plan.clear()
            } else {
                instructionInstance.plan = []
            }
            params.plan.each {
                if (it.value == 'on') {
                    instructionInstance.plan << it.key
                }
            }

            if (instructionInstance.plan.size() == 0) {
                render(view: "edit", model: [instructionInstance: instructionInstance])
                flash.message = "You need to select at least one plan"
                return
            }

            if (!instructionInstance.hasErrors() && instructionInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'instruction.label', default: 'Instruction'), instructionInstance.id])}"
                redirect(action: "show", id: instructionInstance.id)
            }
            else {
                render(view: "edit", model: [instructionInstance: instructionInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def instructionInstance = Instruction.get(params.id)
        if (instructionInstance) {
            if (!springSecurityService.hasValidNa(instructionInstance.na)) {// Is the user authorised to use this service ?
                response 401
                forward(action: "show")
            }
            try {
                instructionInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
            redirect(action: "list")
        }
    }

    protected runMethod() {

        def instructionInstance = serviceAvailable()
        if (instructionInstance) {
            instructionInstance.task.name = OrUtil.camelCase([controllerName, actionName])
            workflowActiveService.first(instructionInstance)
            if (instructionInstance.save(flush: true)) {
                try {
                    sendMessage("activemq:status", instructionInstance.id)
                }
                catch (Exception e) {
                    // message queue may be down
                    log.warn e.message
                }
            }
            redirect(controller: 'instruction', action: 'list')
        }
    }

    protected Instruction serviceAvailable() {

        def instructionInstance = instructionAvailable()
        if (instructionInstance?.status == 200) {
            def taskName = OrUtil.camelCase([controllerName, actionName])
            println("Task name : " + taskName)
            if (!instructionInstance.services.(taskName)) { // Is this service open \ unlocked ?
                render status: 403
            }
        }
        instructionInstance
    }

    protected Instruction instructionAvailable() {

        def instructionInstance = Instruction.get(params.id)// Is this item available ?
        if (!instructionInstance) {
            render status: 404
        } else if (!springSecurityService.hasValidNa(instructionInstance.na)) {// Is the user authorised to use this service ?
            instructionInstance.status = 403
            render status: instructionInstance.status
        } else {
            instructionInstance.status = 200
        }
        instructionInstance
    }
}
