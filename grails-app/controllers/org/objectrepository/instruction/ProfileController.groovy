package org.objectrepository.instruction

import grails.plugins.springsecurity.Secured
import org.objectrepository.security.Policy
import org.objectrepository.util.OrUtil
import org.springframework.dao.DataIntegrityViolationException

import org.objectrepository.security.NamingAuthorityInterceptor

@Secured(['ROLE_OR_USER'])
class ProfileController extends NamingAuthorityInterceptor{

    def springSecurityService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        forward(action: "list", params: params)
    }

    def list() {

        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def profiles = Profile.findAllByNa(params.na, params)
            forward(action: 'show', id: profiles[0].id)
    }

    def show() {
        def profileInstance = Profile.get(params.id)
        if (!profileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'profile.label', default: 'Profile'), params.id])
            redirect(action: "list")
            return
        }
        [profileInstance: profileInstance]
    }

    def edit() {
        def profileInstance = Profile.get(params.id)
        if (!profileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'profile.label', default: 'Profile'), params.id])
            redirect(action: "list")
            return
        }

        def policyInstanceList = Policy.findAllByNa(profileInstance.na)
        [profileInstance: profileInstance, policyList: policyInstanceList.access]
    }

    def update() {
        def profileInstance = Profile.get(params.id)
        if (!profileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'profile.label', default: 'Profile'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (profileInstance.version > version) {
                profileInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'profile.label', default: 'Profile')] as Object[],
                        "Another user has updated this Profile while you were editing")
                render(view: "edit", model: [profileInstance: profileInstance])
                return
            }
        }

        profileInstance.properties = params
        profileInstance.action = params.action1 // needed to avoid confusion with the controller 'action'
        profileInstance.plan.clear()
        params.plan.each {
            if (it.value == 'on') profileInstance.plan << it.key
        }

        if (profileInstance.plan.size() == 0) {
            render(view: "edit", model: [profileInstance: profileInstance])
            flash.message = "You need to select at least one plan"
            return
        }

        def policyInstanceList = Policy.findAllByNa(profileInstance.na)
        if (!profileInstance.save(flush: true)) {
            render(view: "edit", model: [profileInstance: profileInstance, policyList: policyInstanceList.access])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'profile.label', default: 'Profile'), profileInstance.id])
        render(view: "show", model: [profileInstance: profileInstance, policyList: policyInstanceList.access])
    }

    def delete() {
        def profileInstance = Profile.get(params.id)
        if (!profileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'profile.label', default: 'Profile'), params.id])
            redirect(action: "list")
            return
        }

        try {
            profileInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'profile.label', default: 'Profile'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'profile.label', default: 'Profile'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
}
