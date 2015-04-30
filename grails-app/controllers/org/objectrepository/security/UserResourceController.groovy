package org.objectrepository.security

import grails.plugin.springsecurity.SpringSecurityUtils
import org.apache.commons.lang.RandomStringUtils
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured

/**
 *  UserResourceController
 *
 *  There is no domain class for UserResource. Rather it is an embedded part of the User domain class
 *  The reference to the User instance will be 'id'
 *  The reference to the UserResource the 'pid'
 *
 *  We will translate the embedded class into a list here for our view.
 */
@Secured(['ROLE_OR_USER'])
class UserResourceController extends InterceptorValidation {

    static allowedMethods = [save: "POST", update: "POST"]
    def springSecurityService
    def gridFSService

    /**
     * List all pid values.... no limits
     * @param id user id
     * @return
     */
    def list(User userInstance) {
        if (!status(userInstance)) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            return redirect(url: '/' + params.na + '/user/list/' + id)
        }
        respond userInstance, model: [userInstance: userInstance, userResourceInstanceTotal: userInstance.resources.size()]
    }

    def create(User userInstance) {

        if (!status(userInstance)) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            return redirect(url: '/' + params.na + '/user/list/' + id)
        }

        userInstance.resources = [new UserResource(pid: params.na + '/')]
        respond userInstance, model: [userInstance: userInstance, userResourceInstance: userInstance.resources[0]]
    }

    def save(User userInstance) {

        switch (request.format) {
            case 'html':
            case 'form':
                // ok
                break
            default:
                //ToDO: answer to ourselves if this can be prevented via the controller
                render status: HttpStatus.BAD_REQUEST, text: 'Accept not allowed.'
                return
        }

        if (!params._expirationDateEnable) params.remove('expirationDate')
        def userResourceInstance = new UserResource(params)
        userResourceInstance.buckets = params.buckets.findAll {
            it.value == 'on'
        }.collect {
            it.key
        }

        switch (userResourceInstance.validate()) {
            case true:
                saveResource(userInstance, userResourceInstance)
                break
            case false:
                flash.message = message(code: 'default.validation.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
                respond(userResourceInstance, view: 'create')
                break
        }
    }

    void saveResource(User userInstance, UserResource userResourceInstance) {

        final String prefix = userResourceInstance.pid[0..userResourceInstance.pid.indexOf('/') - 1]

        final authorities = SpringSecurityUtils.authoritiesToRoles(springSecurityService.authentication.authorities).findAll {
            it.startsWith('ROLE_OR_USER_')
        }
        if (!('ROLE_OR_USER_' + prefix in authorities)) {
            flash.message = "UserResource is not under your control. The PID value must start with: " + authorities.collect {
                it[13..-1]
            }.join(', ')
            respond(userResourceInstance, status: HttpStatus.FORBIDDEN, view: "create", model: [userInstance: userInstance, userResourceInstance: userResourceInstance])
            return
        }

        if (userResourceInstance.expirationDate && userResourceInstance.expirationDate < new Date())
            userResourceInstance.expirationDate = null

        final resource = gridFSService.countPidOrObjId(params.na, userResourceInstance.pid)

        if (!resource.folders) {
            flash.message = "Unknown resource: " + userResourceInstance.pid
            respond(userResourceInstance, status: HttpStatus.NOT_FOUND, view: "create", model: [userInstance: userInstance, userResourceInstance: userResourceInstance])
            return
        }

        userResourceInstance.folders = resource.folders
        userResourceInstance.thumbnail = (resource.orfile.level3) ? true : false
        userResourceInstance.contentType = resource.orfile.master.contentType
        userResourceInstance.objid = resource.orfile.master.metadata.objid


        userInstance.resources?.removeAll {
            it.pid == userResourceInstance.pid
        }
        userInstance.resources << userResourceInstance
        userInstance.save flush: true
        respond userInstance, view: 'list', model: [userInstance: userInstance, userResourceInstanceTotal: userInstance.resources.size()]
    }

    def edit(User userInstance) {
        if (!status(userInstance)) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])
            return redirect(url: '/' + params.na + '/user/list/' + params.id)
        }

        def userResourceInstance = userInstance.resources?.find {
            it.pid == params.pid
        }
        if (!userResourceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userResource.label', default: 'Resource'), params.pid])
            return redirect(url: '/' + params.na + '/' + controllerName + '/list/' + userInstance.id)
        }

        respond userInstance, model: [userInstance: userInstance, userResourceInstance: userResourceInstance]
    }

    def update(User userInstance) {
        save(userInstance)
    }

    def delete(User userInstance) {

        switch (status(userInstance)) {
            case HttpStatus.OK:
                if (userInstance.resources?.removeAll {
                    it.pid == params.pid
                }) {
                    userInstance.newpassword = RandomStringUtils.randomAscii(6)
                    // The remove does not register as a field change. Hence we misuse this field.
                }
                userInstance.save flush: true

                flash.message = message(code: 'default.deleted.message', args: [message(code: 'userResource.label', default: 'Resource'), params.pid])
                switch (request.format) {
                    case 'html':
                    case 'form':
                        forward(action: 'list')
                        break
                    default:
                        respond userInstance
                        break
                }
                break

            case HttpStatus.BAD_REQUEST:
                flash.message = message(code: 'default.validation.message', args: [message(code: 'userResource.label', default: 'Resource'), params.pid])
                respond(userInstance, forward(action: 'show', id: userInstance.id))
                break
        }
    }
}