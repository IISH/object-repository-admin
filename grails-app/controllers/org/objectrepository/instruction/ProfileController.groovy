package org.objectrepository.instruction

import grails.plugin.springsecurity.annotation.Secured
import org.objectrepository.security.InterceptorValidation
import org.objectrepository.security.Policy
import org.springframework.http.HttpStatus

@Secured(['ROLE_OR_USER'])
class ProfileController extends InterceptorValidation {

    def springSecurityService

    static allowedMethods = [save: 'POST', update: 'PUT']

    def index() {
        render view: 'show'
        show(Profile.findByNa(params.na))
    }

    def show(Profile profileInstance) {

        switch (status(profileInstance)) {
            case HttpStatus.OK:
            case HttpStatus.BAD_REQUEST:
                respond profileInstance
                break
        }
    }

    def edit(Profile profileInstance) {

        switch (status(profileInstance)) {
            case HttpStatus.OK:
                def policyInstanceList = Policy.findAllByNa(profileInstance.na)
                respond profileInstance, model: [profileInstance: profileInstance, policyList: policyInstanceList.access]
                break

            case HttpStatus.BAD_REQUEST:
                respond(profileInstance, view: 'show', model: [profileInstance: profileInstance])
                break
        }
    }

    def update(Profile profileInstance) {

        def policyInstanceList = null
        if (profileInstance) {
            policyInstanceList = Policy.findAllByNa(profileInstance.na)
            profileInstance.properties = params
            profileInstance.action = params.action1 // needed to avoid confusion with the controller 'action'

            if (!profileInstance.plan) {
                flash.message = 'You need to select at least one plan'
                response.status = HttpStatus.BAD_REQUEST.value()
                return respond(profileInstance, view: 'edit', model: [profileInstance: profileInstance, policyList: policyInstanceList.access])
            }
        }

        switch (status(profileInstance)) {
            case HttpStatus.OK:
                profileInstance.save flush: true
                flash.message = message(code: 'default.updated.message', args: [message(code: 'profile.label', default: 'Profile'), profileInstance.id])
                respond(profileInstance, view: 'show', model: [profileInstance: profileInstance, policyList: policyInstanceList.access])
                break

            case HttpStatus.FORBIDDEN:
            case HttpStatus.NOT_FOUND:
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'profile.label', default: 'Profile'), params.id])
                respond(profileInstance, view: 'edit', model: [profileInstance: profileInstance, policyList: policyInstanceList.access])
                break

            case HttpStatus.BAD_REQUEST:
                respond(profileInstance, view: 'edit', model: [profileInstance: profileInstance, policyList: policyInstanceList.access])
                break
        }
    }
}
