package org.objectrepository.security

import org.apache.commons.lang.RandomStringUtils
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.ClientToken
import org.springframework.security.oauth2.provider.OAuth2Authentication

@Secured(['ROLE_OR_USER'])
class UserController extends NamingAuthorityInterceptor {

    static allowedMethods = [save: "POST", update: "POST"]

    def springSecurityService
    def gridFSService
    def ldapUserDetailsManager

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        final list = User.findAllByNa(params.na)
        [userInstanceList: list, userInstanceTotal: list.size()]
    }

    def create() {
        [userInstance: new User(params)]
    }

    def save() {

        // Set password when it was left empty
        final def newPassword
        if (params.password == "" && params.confirmpassword == "") {
            newPassword = RandomStringUtils.random(6, true, false)
            params.password = newPassword
            params.confirmpassword = newPassword
        } else
            newPassword = params.password
        params.username = params.username.toLowerCase()

        if (User.findByUsername(params.username)) {
            flash.message = "Account already exists. Choose a different name."
            render(view: "create", model: [userInstance: params])
            return
        }

        // confirm password check
        if (!params.skippassword) {
            if (params.password != params.confirmpassword) {
                flash.message = "Passwords do not match"
                render(view: "create", model: [userInstance: params])
                return
            }
        }
        params.password = springSecurityService.encodePassword(params.password, UUID.randomUUID().encodeAsMD5Bytes())

        def userInstance = new User(params)
        if (!userInstance.save(flush: true)) {
            render(view: "create", model: [userInstance: userInstance])
            return
        }

        ldapUserDetailsManager.replacekey(userInstance)

        if (params.sendmail) {
            sendMail {
                to params.mail
                from grailsApplication.config.mail.from
                subject message(code: "mail.user.created.subject")
                body message(code: "mail.user.created." + userInstance.useFor, args: [springSecurityService.principal.username,
                        grailsApplication.config.grails.serverURL,
                        grailsApplication.config.mail.sftpServer,
                        params.id,
                        newPassword])
            }
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        forward(action: "show", id: userInstance.id)
    }

    def show(Long id) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
            return
        }

        def token = ldapUserDetailsManager.selectKeys(userInstance.username)
        if (!token) token = ldapUserDetailsManager.replacekey(userInstance)
        [userInstance: userInstance, token: token]
    }

    def edit(Long id) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
        } else
            [userInstance: userInstance]
    }

    def update(Long id, Long version) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
            return
        } else if (version != null) {
            if (userInstance.version > version) {
                userInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'user.label', default: 'User')] as Object[],
                        "Another ftp has updated this User while you were editing")
                render(view: "edit", model: [userInstance: userInstance])
                return
            }
        }

        userInstance.properties = params

        if (!userInstance.save(flush: true)) {
            render(view: "edit", model: [userInstance: userInstance])
            return
        }

        ldapUserDetailsManager.replacekey(userInstance)

        flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        forward(action: "show", id: userInstance.id)
    }

    def updatekey(Long id) {

        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
            return
        }

        ldapUserDetailsManager.replacekey(userInstance)
        forward(action: "show", id: params.id)
    }

    def delete(Long id) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
            return
        }

        try {
            userInstance.delete(flush: true)
            ldapUserDetailsManager.removeToken(ldapUserDetailsManager.selectKeys(userInstance.username))
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            log.warn(e)
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "show", id: id)
        }
    }

}
