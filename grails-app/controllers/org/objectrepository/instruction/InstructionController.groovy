package org.objectrepository.instruction

import grails.converters.XML
import org.objectrepository.security.NamingAuthorityInterceptor
import org.objectrepository.security.Policy
import org.objectrepository.util.OrUtil
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class InstructionController extends NamingAuthorityInterceptor {

    def grailsApplication
    def springSecurityService
    def workflowActiveService
    def downloadService

    static allowedMethods = [update: "POST", uploadfile: "POST"]

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

        def instructionInstanceList = (params.objid) ? Instruction.findAllByNaAndFileSetLike(params.na, params.objid, params) : Instruction.findAllByNa(params.na, params)
        int count = (params.objid) ? 1 : Instruction.countByNa(params.na)

        if (params.view) {
            render(view: params.view, model: [instructionInstanceList: instructionInstanceList, instructionInstanceTotal: count])
            params.remove('view')
        } else
            [instructionInstanceList: instructionInstanceList, instructionInstanceTotal: count]
    }

    def show = {
        params.view = 'show'
        forward(action: "showremote", params: params)
    }

    def showremote = {
        def instructionInstance = serviceAvailable()
        if (!instructionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
            forward(action: 'list')
        } else if (instructionInstance.status == 200) {
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
            forward(action: 'list')
        } else if (params.instruction) {
            instructionInstance.delete(flush: true)
            forward(action: 'list')
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
            forward(action: 'list')
        }
        [instructionInstance: instructionInstance]
    }

    def edit = {

        def instructionInstance = serviceAvailable()
        if (!instructionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id]) }"
            forward(action: 'list')
        } else if (instructionInstance.status == 200) {
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

            if (!instructionInstance.plan) {
                render(view: "edit", model: [instructionInstance: instructionInstance])
                flash.message = "You need to select at least one plan"
                return
            }
            final username = springSecurityService.principal.username.toLowerCase()
            if (instructionInstance.approval == null)
                instructionInstance.approval = []
            if (!(username in instructionInstance.approval))
                instructionInstance.approval << username

            if (!instructionInstance.hasErrors() && instructionInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'instruction.label', default: 'Instruction'), instructionInstance.id])}"
                forward(action: 'show', id: instructionInstance.id)
            } else {
                render(view: "edit", model: [instructionInstance: instructionInstance])
            }
        } else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
            forward(action: 'list')
        }
    }

    def delete = {
        def instructionInstance = Instruction.get(params.id)
        if (instructionInstance) {
            try {
                instructionInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
                forward(action: 'list')
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
                forward(action: 'show', id: params.id)
            }
        } else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'instruction.label', default: 'Instruction'), params.id])}"
            forward(action: 'list')
        }
    }

    def status = {
        def instructionInstance = Stagingfile.findByPidAndNa(params.pid, params.na)?.parent
        if (instructionInstance) {
            response.setCharacterEncoding('utf-8')
            response.setContentType('text/xml')
            def instructionByteArray = OrUtil.makeOrType(instructionInstance, ['version', 'pidwebserviceKey']).bytes
            response.setContentLength(instructionByteArray.size())
            response.outputStream << instructionByteArray
            response.outputStream.flush()
            response.outputStream.close()
        } else {
            return render(status: 404, characterEncoding: 'utf-8', contentType: 'text/xml')
        }
    }

    protected runMethod() {
        def instructionInstance = serviceAvailable()
        if (instructionInstance?.status == 200) {
            instructionInstance.task.name = OrUtil.camelCase([controllerName, actionName])
            instructionInstance.task.taskKey()
            workflowActiveService.first(instructionInstance)
            if (instructionInstance.save(flush: true)) {
                try {
                    sendMessage("activemq:status", instructionInstance.task.identifier)
                }
                catch (Exception e) {
                    log.warn e
                }
            }
        }
        forward(action: 'show', id: params.id)
    }

    /**
     * serviceAvailable
     *
     * Returns the instruction if it exists.
     * Adds a status code to indicate if the service is available ( 200 ) or locked ( 403 )
     *
     * @return
     */
    protected Instruction serviceAvailable() {

        def instructionInstance = Instruction.get(params.id)// Is this item available ?
        if (instructionInstance) {
            instructionInstance.status = 200
            def taskName = OrUtil.camelCase([controllerName, actionName])
            if (taskName in grailsApplication.config.plans) {
                if (!instructionInstance.services.find { it.name == taskName }) {
                    instructionInstance.status = 403
                    flash.message = "Service locked."
                }
            }
        }
        instructionInstance
    }
}