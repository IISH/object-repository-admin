package org.objectrepository.security

import org.apache.commons.lang.RandomStringUtils
import org.springframework.security.access.annotation.Secured

/**
 * UserController
 *
 * We will use LDAP for creating ftp accounts. Not user accounts.
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 */
@Secured(['ROLE_OR_USER'])
class StagingareaController extends NamingAuthorityInterceptor {

    def springSecurityService
    def ldapUserDetailsManager

    static allowedMethods = [save: "POST", update: "POST", changekey: "POST"]

    def index = {
        forward(action: "list", params: params)
    }

    def list = {

        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def userInstanceList = ldapUserDetailsManager.listLdapUsers(params.na)
        [userInstanceList: userInstanceList, userInstanceTotal: userInstanceList.size()]
    }

    def create = {
        [userInstance: []]
    }

    def save = {

        // Set password when it was left empty
        final def newPassword
        if (params.password == "" && params.confirmpassword == "") {
            newPassword = RandomStringUtils.random(6, true, false)
            params.password = newPassword
            params.confirmpassword = newPassword
        } else
            newPassword = params.password
        params.username = params.username.toLowerCase()

        params.id = params.username
        if (ldapUserDetailsManager.userExists(params.id)) {
            flash.message = "User already has an account in LDAP. Choose a different name."
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
        ldapUserDetailsManager.updateUser(params, true)
        if (params.sendmail) {
            sendMail {
                to params.mail
                from grailsApplication.config.mail.from
                subject message(code: "mail.user.created.subject")
                body message(code: "mail.user.created.stagingarea", args: [springSecurityService.principal.username,
                        grailsApplication.config.grails.serverURL,
                        grailsApplication.config.mail.sftpServer,
                        params.id,
                        newPassword])
            }
        }

        forward(action: "show", id: params.id)
    }

    def show = {
        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            forward(action: "list")
            return
        }
        [userInstance: userInstance]
    }

    def edit = {
        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            forward(action: "list")
        } else
            [userInstance: userInstance]
    }

    def update = {
        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            forward(action: "list")
        } else if (!ldapUserDetailsManager.manages(userInstance, params.na)) {
            flash.message = "Cannot set this ftp account; as it does not belong to your naming authority."
            render(view: "edit", model: [userInstance: userInstance])
        } else if (params.confirmpassword && params.confirmpassword != params.password) {
            flash.message = "Passwords do not match"
            render(view: "edit", model: [userInstance: userInstance])
        } else {
            boolean passwordAltered = (params.confirmpassword != "" && params.password != userInstance.password)
            params.password = (passwordAltered) ?
                springSecurityService.encodePassword(params.password, UUID.randomUUID().encodeAsMD5Bytes()) :
                userInstance.password
            params.uidNumber = userInstance.uidNumber
            ldapUserDetailsManager.updateUser(params, false)
            flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            forward(action: "show", id: userInstance.id)
        }
    }

    def delete = {
        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            forward(action: "list")
        } else if (userInstance.id == springSecurityService.principal.username) {
            render(message(code: "delete.self"))
        } else
            try {
                ldapUserDetailsManager.deleteUser(params.id)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])}"
                forward(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.warn e.message
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])}"
                forward(action: "show", id: params.id)
            }
    }

}