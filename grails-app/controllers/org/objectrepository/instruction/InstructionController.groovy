package org.objectrepository.instruction

import org.objectrepository.security.Policy
import org.objectrepository.util.OrUtil
import grails.plugins.springsecurity.Secured

import org.objectrepository.security.NamingAuthorityInterceptor

@Secured(['IS_AUTHENTICATED_FULLY'])
class InstructionController extends NamingAuthorityInterceptor {

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

        def instructionInstanceList = Instruction.findAllByNa(params.na, params)
        int count = Instruction.countByNa(params.na)

        if (params.view) {
            render(view: params.view, model: [instructionInstanceList: instructionInstanceList, instructionInstanceTotal: count])
            params.remove('view')
        }
        else
            [instructionInstanceList: instructionInstanceList, instructionInstanceTotal: count]
    }

    def show = {
        params.view = 'show'
        forward(action: "showremote", params: params)
    }

    def showremote = {
        def instructionInstance = instructionAvailable()
        if (!instructionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
            //redirect uri: createLink( base: '/' + params.na, action: "list")
            forward(action:'list')
        }
        else if (instructionInstance.status == 200) {
            OrUtil.setInstructionPlan(instructionInstance)
            render(view: params.view ?: '_showremote', model: [instructionInstance: instructionInstance])
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
            //redirect uri: createLink( base: '/' + params.na, controller: controllerName, action: "list")
            forward(action:'list')
        } else if (params.instruction) {
            instructionInstance.delete(flush: true)
           // redirect uri: createLink( base: '/' + params.na, controller: controllerName, action: "list")
            forward(action:'list')
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
                response.addHeader('Content-disposition', 'attachment; filename="instruction.xml"')
                writer = response.outputStream
            } else {
                final File file = new File(instructionInstance.fileSet, "instruction.xml")
                writer = file.newWriter('UTF-8')
            }
            downloadService.write(writer, instructionInstance)
            //redirect uri: createLink( base: '/' + params.na, controller: controllerName, action: "list")
            forward(action:'list')
        }
        [instructionInstance: instructionInstance]
    }

    def edit = {

        def instructionInstance = Instruction.get(params.id)
        if (!instructionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id]) }"
            //redirect uri: createLink( base: '/' + params.na, controller: controllerName, action: "list")
            forward(action:'list')
        }
        else if (instructionInstance.status == 200) {
            OrUtil.setInstructionPlan(instructionInstance)
            def policyInstanceList = Policy.findAllByNa(instructionInstance.na)
            [instructionInstance: instructionInstance, policyList: policyInstanceList.access]
        }
    }

    def update = {
        def instructionInstance = Instruction.get(params.id)
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
                //redirect uri: createLink( base: '/' + params.na, action: "show", id: instructionInstance.id)
                forward(action:'show', id: instructionInstance.id)
            }
            else {
                render(view: "edit", model: [instructionInstance: instructionInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
            //redirect uri: createLink( base: '/' + params.na, action: "list")
            forward(action:'list')
        }
    }

    def delete = {
        def instructionInstance = Instruction.get(params.id)
        if (instructionInstance) {
            try {
                instructionInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
               // redirect uri: createLink( base: '/' + params.na, action: "list")
                forward(action:'list')
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
              //  redirect uri: createLink( base: '/' + params.na, id:params.id, controller: 'show')
                forward(action:'show', id:params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
            //redirect uri: createLink( base: params.na, action: "list")
            forward(action:'list')
        }
    }

    protected runMethod() {

        def instructionInstance = serviceAvailable()
        if (instructionInstance) {
            instructionInstance.task.name = OrUtil.camelCase([controllerName, actionName])
            instructionInstance.task.taskKey()
            workflowActiveService.first(instructionInstance)
            if (instructionInstance.save(flush: true)) {
                try {
                    sendMessage("activemq:status", instructionInstance.task.identifier)
                }
                catch (Exception e) {
                    // message queue may be down
                    log.warn e.message
                }
            }
            //redirect uri: createLink( base: '/' + params.na, action: "show", id:params.id)
            forward(action:'show', id:params.id)
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
        } else {
            instructionInstance.status = 200
        }
        instructionInstance
    }

}
