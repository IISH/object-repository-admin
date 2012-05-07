package org.objectrepository.security

import grails.plugins.springsecurity.Secured
import org.springframework.dao.DataIntegrityViolationException

@Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
class PolicyController {

    def springSecurityService
    def policyService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        forward(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def policies = (springSecurityService.hasRole('ROLE_ADMIN')) ? Policy.list(params) : Policy.findAllByNa(springSecurityService.principal.na, params)
        if (!policies) {
            // No policy yet for this organization. This required an administrator setting a NA.
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
            return;
        }
        [policyInstanceList: policies, policyInstanceTotal: policies.size()]
    }

    def create() {
        def buckets = grailsApplication.config.accessMatrix['closed'].collect {
            new Bucket(it)
        }
        Policy policy = new Policy(access: 'add a custom value here', na: springSecurityService.principal.na, buckets: buckets)
        [policyInstance: policy, accessStatus: grailsApplication.config.accessStatus]

    }

    def save() {
        def policyInstance = new Policy(params)
        policyInstance.na = springSecurityService.principal.na
        policyInstance.buckets = grailsApplication.config.accessMatrix['closed'].collect { it ->
            new Bucket(bucket: it.bucket, access: params[it.bucket])
        }
        if (!policyInstance.save(flush: true)) {
            render(view: "create", model: [policyInstance: policyInstance])
            return
        }
        flash.message = message(code: 'default.created.message', args: [message(code: 'policy.label', default: 'Policy'), policyInstance.id])
        forward(action: "show", id: policyInstance.id)
    }

    def show() {
        def policyInstance = Policy.get(params.id)
        if (!policyInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
            forward(action: "list")
            return
        }
        if (!springSecurityService.hasValidNa( policyInstance.na )) { // Make sure we do not see anothers na here...
            response 403
            forward(action: "list")
        }
        [policyInstance: policyInstance]
    }

    def edit() {
        def policyInstance = Policy.get(params.id)
        if (!policyInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
            forward(action: "list")
        }
        else if (accessMatrixIsLocked(policyInstance.access)) {
            forward(action: "show", params: params)
        }
        else if (!springSecurityService.hasValidNa(policyInstance.na)) {
            response 403
            forward(action: "list")
        }
        [policyInstance: policyInstance, accessStatus: grailsApplication.config.accessStatus]
    }

    def update() {
        def policyInstance = Policy.get(params.id)
        if (!policyInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
            forward(action: "list")
            return
        }
        if (!springSecurityService.hasValidNa(policyInstance.na)) {
            response 403
            forward(action: "list")
        }

        policyInstance.buckets = grailsApplication.config.accessMatrix['closed'].collect { it ->
            new Bucket(bucket: it.bucket, access: params[it.bucket])
        }

        if (!policyInstance.save(flush: true)) {
            render(view: "edit", model: [policyInstance: policyInstance])
            return
        }

        policyService.setPolicy(springSecurityService.principal.na, policyInstance)

        flash.message = message(code: 'default.updated.message', args: [message(code: 'policy.label', default: 'Policy'), policyInstance.id])
        forward(action: "show", id: policyInstance.id)
    }

    def delete() {
        def policyInstance = Policy.get(params.id)
        if (!policyInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
            forward(action: "list")
            return
        }

        if (accessMatrixIsLocked(policyInstance.access)) {
            forward(action: "show", params: params)
            return
        }

        if (!springSecurityService.hasValidNa(policyInstance.na)) {
            response 403
            forward(action: "list")
        }
        else
            try {
                policyInstance.delete(flush: true)
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
                forward(action: "list")
            }
            catch (DataIntegrityViolationException e) {
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
                forward(action: "show", id: params.id)
            }
    }

    /**
     * accessMatrixIsLocked
     *
     * We do not allow for editing the default access matrix settings
     *
     * @param access
     * @return
     */
    boolean accessMatrixIsLocked(def access) {

        def locked = (grailsApplication.config.accessMatrix[access])
        if (locked) {
            flash.message = message(code: 'Policy.access.locked')
        }
        locked
    }
}
