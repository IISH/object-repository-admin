package org.objectrepository.files

import grails.plugins.springsecurity.Secured

import org.objectrepository.security.Policy

@Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
class OrfileController {

    def springSecurityService
    def gridFSService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        forward(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.offset = params.offset ? params.int('offset') : 0
        params.order = !params.order || params.order == "asc" ? 1 : -1
        params.sort = params.sort ?: "_id"
        if (params.label && params.label == 'everything') params.label = null
        final String na = ( springSecurityService.hasRole('ROLE_ADMIN') ) ? params.na : springSecurityService.principal.na
        def orfileInstanceList = gridFSService.findAllByNa(na, params)
        def labels = ['everything']
        gridFSService.labels(na).each {
            if (it.label) labels << it.label
        }
        [orfileInstanceList: orfileInstanceList, orfileInstanceListTotal: gridFSService
                .countByNa(na, params),
                labels: labels]
    }

    def show() {
        final String na = ( springSecurityService.hasRole('ROLE_ADMIN') ) ? params.na : springSecurityService.principal.na
        def orfileInstance = gridFSService.get(na, params.id)
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), params.id])
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
        final String na = ( springSecurityService.hasRole('ROLE_ADMIN') ) ? params.na : springSecurityService.principal.na
        def orfileInstance = gridFSService.get(na, params.id)
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), params.id])
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
        final String na = ( springSecurityService.hasRole('ROLE_ADMIN') ) ? params.na : springSecurityService.principal.na
        def orfileInstance = gridFSService.get(na, params.id)
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), params.id])
            redirect(action: "list")
            return
        }

        if (!springSecurityService.hasValidNa(na)) {
            response 401
            forward(action: "list")
        }

        orfileInstance.metadata.access = params.access
        orfileInstance.metadata.label = params.label
        orfileInstance.metadata.label = params.label
        orfileInstance.metadata.cache.get(0).metadata.access = params.access
        orfileInstance.metadata.cache.get(0).metadata.label = params.label

        gridFSService.update(orfileInstance, params)

        flash.message = message(code: 'default.updated.message', args: [message(code: 'files.label', default: 'Files'), orfileInstance.id])
        redirect(action: "show", id: orfileInstance.id)
    }

    def delete() {
        def filesInstance = Orfile.get(params.id)
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

    def download() {

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/xml")
        response.addHeader("content-disposition", "attachment; filename=orfiles.xml")
        gridFSService.writeOrfiles(params, springSecurityService.principal.na, response.outputStream)
    }
}
