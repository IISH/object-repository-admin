package org.objectrepository.files

import grails.plugins.springsecurity.Secured
import org.objectrepository.domain.Orfiles
import org.objectrepository.security.Policy

@Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
class OrfilesController {

    def springSecurityService
    def gridFSService
    def mongo
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]


    def index() {
        forward(action: "list", params: params)
    }

    def list() {

        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def orfilesInstanceList = gridFSService.findAllByNa(springSecurityService.principal.na, params)
        [orfilesInstanceList: orfilesInstanceList, orfilesInstanceListTotal: orfilesInstanceList.size()]
    }

    def show() {
        def orfilesInstance = gridFSService.get(springSecurityService.principal.na, params.id)
        if (!orfilesInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), params.id])
            redirect(action: "list")
            return
        }
        [orfilesInstance: orfilesInstance]
    }

    def edit() {
        def filesInstance = Orfiles.get(params.id)
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
        def filesInstance = Orfiles.get(params.id)
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
        def filesInstance = Orfiles.get(params.id)
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
