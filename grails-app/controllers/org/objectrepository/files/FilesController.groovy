package org.objectrepository.files

import org.objectrepository.security.Policy
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
class FilesController {

    def springSecurityService
    def filesUDService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        forward(action: "list", params: params)
    }

    def list() {

        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def filesInstanceList = springSecurityService.hasRole('ROLE_ADMIN') ? Files.list(params) : Files.findAllByNa(springSecurityService.principal.na, params)
        [filesInstanceList: filesInstanceList, filesInstanceTotal: filesInstanceList.size()]
    }

    def show() {
        def filesInstance = Files.get(params.id)
        if (!filesInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), params.id])
            redirect(action: "list")
            return
        }
        [filesInstance: filesInstance]
    }

    def edit() {
        def filesInstance = Files.get(params.id)
        if (!filesInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), params.id])
            forward(action: "list")
            return
        }

        if (!springSecurityService.hasValidNa(filesInstance.na)) {
            response 401
            forward(action: "list")
        }

        def policyInstanceList = Policy.findAllByNa(filesInstance.na)
        [filesInstance: filesInstance, policyList: policyInstanceList.access]
    }

    /*def create() {
        println("Method not implemented")
    }*/

    def update() {
        def filesInstance = Files.get(params.id)
        if (!filesInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), params.id])
            redirect(action: "list")
            return
        }

        if (!springSecurityService.hasValidNa(filesInstance.na)) {
            response 401
            forward(action: "list")
        }

        filesUDService.update(filesInstance.pid, params)

        flash.message = message(code: 'default.updated.message', args: [message(code: 'files.label', default: 'Files'), filesInstance.id])
        redirect(action: "show", id: filesInstance.id)
    }

    def delete() {
        def filesInstance = Files.get(params.id)
        if (!filesInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), params.id])
            redirect(action: "list")
            return
        }

        if (!springSecurityService.hasValidNa(filesInstance.na)) {
            response 401
            forward(action: "list")
        }

        filesUDService.delete(filesInstance.pid)
        flash.message = message(code: 'default.deleted.message', args: [message(code: 'files.label', default: 'Files'), filesInstance.id])
        redirect(action: "show", id: filesInstance.id)
    }
}
