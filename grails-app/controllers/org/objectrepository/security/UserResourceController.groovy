package org.objectrepository.security

import grails.converters.JSON
import grails.converters.XML
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
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
class UserResourceController extends NamingAuthorityInterceptor {

    static allowedMethods = [save: "POST", update: "POST"]
    def springSecurityService
    def ldapUserDetailsManager
    def gridFSService

    /**
     * List all pid values.... no limits
     * @param id user id
     * @return
     */
    def list(Long id) {
        final User userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(url: '/' + params.na + '/user/list/' + id)
            return
        }
        [userInstance: userInstance, userResourceInstanceList: userInstance.resources, userResourceInstanceTotal: userInstance.resources.size()]
    }

    def create(Long id) {

        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(url: '/' + params.na + '/user/list/' + id)
            return
        }

        final resource = new UserResource(pid: params.na + '/')
        [userInstance: userInstance, userResourceInstance: resource]
    }

    def save(Long id) {

        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(url: '/' + params.na + '/user/list/' + id)
            return
        }

        params.downloads = 0
        def userResourceInstance = new UserResource(params)
        final String pid = params.pid
        final String prefix = pid[0..pid.indexOf('/') - 1]

        final authorities = SpringSecurityUtils.authoritiesToRoles(springSecurityService.authentication.authorities).findAll {
            it.startsWith('ROLE_OR_USER_')
        }
        if (!('ROLE_OR_USER_' + prefix in authorities)) {
            flash.message = "Resource is not under your control. The PID value must start with: " + authorities.collect { it[13..-1] }.join(', ')
            render(view: "create", model: [userInstance: userInstance, userResourceInstance: userResourceInstance])
            return
        }

        if (userResourceInstance.expirationDate && userResourceInstance.expirationDate < new Date())
            userResourceInstance.expirationDate = null

        final resource = gridFSService.countPidOrObjId(params.na, pid)
        if (!resource.locations) {
            flash.message = "Unknown resource: " + pid
            render(view: "create", model: [userInstance: userInstance, userResourceInstance: userResourceInstance])
            return
        }

        userResourceInstance.thumbnail = (resource.orfile.level3) ? true : false
        userResourceInstance.contentType = resource.orfile.master.contentType
        userResourceInstance.objid = resource.orfile.master.metadata.objid
        userResourceInstance.buckets = params.buckets.findAll {
            it.value == 'on'
        }.collect {
            it.key
        }

        resource.orfile.each { orfile ->
            resource.locations.each {
                def location = params.na + '/' + orfile.key + it
                listDirectories(userResourceInstance.folders, (location =~ /\/\d{4}-\d{2}-\d{2}\//).replaceFirst(''))
            }
        }

        userInstance.resources?.removeAll {
            it.pid == pid
        }
        userInstance.resources << userResourceInstance

        if (!userInstance.save(flush: true)) {
            render(view: "create", model: [userInstance: userInstance, userResourceInstance: userResourceInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'userResource.label', default: 'UserResource'), pid])
        redirect(url: '/' + params.na + '/' + controllerName + '/list/' + id)
    }

    static void listDirectories(def list, String l) {

        String s = ""
        l.split('/').each {
            s += '/' + it
            if (!(s in list))
                list << s
        }
    }

    def edit(Long id) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(url: '/' + params.na + '/user/list/' + id)
            return
        }

        def userResourceInstance = userInstance.resources?.find {
            it.pid == params.pid
        }
        if (!userResourceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userResource.label', default: 'UserResource'), params.pid])
            redirect(url: '/' + params.na + '/' + controllerName + '/list/' + id)
            return
        }

        [userInstance: userInstance, userResourceInstance: userResourceInstance]
    }

    def update(Long id) {
        save(id)
    }

    def delete(Long id) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(url: '/' + params.na + '/user/list/' + id)
            return
        }

        userInstance.resources?.removeAll {
            it.pid == params.pid
        }

        if (!userInstance.save(flush: true)) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(url: '/' + params.na + '/' + controllerName + '/list/' + id)
            return
        }

        flash.message = message(code: 'default.deleted.message', args: [message(code: 'userResource.label', default: 'UserResource'), params.pid])
        redirect(url: '/' + params.na + '/' + controllerName + '/list/' + id)
    }
}