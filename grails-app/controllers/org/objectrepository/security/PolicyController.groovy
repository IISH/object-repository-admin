package org.objectrepository.security

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class PolicyController extends InterceptorValidation {

    def springSecurityService
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
        forward(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def policies = Policy.findAllByNa(params.na, params)
        respond policies, model: [policyInstanceList: policies, policyInstanceTotal: policies.size()]
    }

    def create() {
        respond new Policy(access: 'add a custom value here', na: params.na, buckets: grailsApplication.config.accessMatrix['closed'].collect {
            new Bucket(it)
        })
    }

    def save(Policy policyInstance) {
        policyInstance.buckets = grailsApplication.config.accessMatrix['closed'].collect {
            new Bucket(bucket: it.bucket, access: params[it.bucket])
        }

        switch (status(policyInstance)) {
            case HttpStatus.OK:
                policyInstance.save()
                flash.message = message(code: 'default.created.message', args: [message(code: 'policy.label', default: 'Policy'), policyInstance.id])
                forward(action: "show", id: policyInstance.id)
                break

            case HttpStatus.BAD_REQUEST:
                respond(policyInstance, view: 'create', model: [policyInstance: policyInstance])
                break
        }
    }

    def show(Policy policyInstance) {

        switch (status(policyInstance)) {
            case HttpStatus.OK:
                respond policyInstance
                break
            case HttpStatus.BAD_REQUEST:
                respond(policyInstance, view: 'create', model: [policyInstance: policyInstance])
                break
        }
    }

    def edit(Policy policyInstance) {

        switch (status(policyInstance)) {
            case HttpStatus.OK:
                if (accessMatrixIsLocked(policyInstance.access))
                    forward(action: "show", params: params)
                else
                    respond policyInstance
                break

            case HttpStatus.BAD_REQUEST:
                respond(policyInstance, view: 'show', model: [policyInstance: policyInstance])
                break
        }
    }

    def update(Policy policyInstance) {

        policyInstance?.buckets = grailsApplication.config.accessMatrix['closed'].collect {
            new Bucket(bucket: it.bucket, access: params[it.bucket])
        }

        switch (status(policyInstance)) {
            case HttpStatus.OK:
                if (accessMatrixIsLocked(policyInstance.access)) {
                    forward(action: "show", params: params)
                } else {
                    policyInstance.save()
                    flash.message = message(code: 'default.updated.message', args: [message(code: 'policy.label', default: 'Policy'), policyInstance.id])
                    forward(action: "show", id: policyInstance.id)
                }
                break

            case HttpStatus.BAD_REQUEST:
                render(view: "edit", model: [policyInstance: policyInstance])
        }

    }

    def delete(Policy policyInstance) {

        switch (status(policyInstance)) {
            case HttpStatus.OK:
                if (accessMatrixIsLocked(policyInstance.access))
                    forward(action: "show", params: params)
                else
                    try {
                        policyInstance.delete(flush: true)
                        flash.message = message(code: 'default.deleted.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
                        forward(action: "list")
                    }
                    catch (DataIntegrityViolationException e) {
                        flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'policy.label', default: 'Policy'), params.id])
                    }
                break

            case HttpStatus.BAD_REQUEST:
                respond(policyInstance, view: 'show', model: [policyInstance: policyInstance])
                break
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
    private boolean accessMatrixIsLocked(def access) {

        def locked = (grailsApplication.config.accessMatrix[access])
        if (locked) {
            flash.message = message(code: 'Policy.access.locked')
        }
        locked
    }
}
