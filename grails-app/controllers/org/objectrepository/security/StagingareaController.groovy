package org.objectrepository.security

import grails.plugins.springsecurity.Secured
import org.apache.commons.lang.RandomStringUtils
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.ClientToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.objectrepository.ai.ldap.LdapUser
import org.objectrepository.ldap.CustomLdapUserDetailsManager

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
    def tokenStore
    def tokenServices
    def clientDetailsService

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
                body message(code: "mail.user.created.body", args: [springSecurityService.principal.username,
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

        def token = tokenStore.selectKeys(params.id)
        if (!token) {
            def client = clientDetailsService.clientDetailsStore.get("clientId")
            ClientToken clientToken = new ClientToken(client.clientId, client.resourceIds as Set<String>,
                    client.clientSecret, client.scope as Set<String>, client.authorizedGrantTypes)
            final OAuth2Authentication authentication = new OAuth2Authentication(clientToken,
                    authentication(params))
            token = tokenServices.createAccessToken(authentication)
        }
        [userInstance: userInstance, token: token]
    }

    def edit = {
        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            forward(action: "list")
        } else
            [userInstance: userInstance]
    }

    def updatekey = {

        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)
        OAuth2AccessToken token = tokenStore.selectKeys(userInstance.username)
        if (token) {
            tokenStore.removeAccessTokenUsingRefreshToken(token.refreshToken.value)
            tokenStore.removeRefreshToken(token.refreshToken.value)
            def client = clientDetailsService.clientDetailsStore.get("clientId")
            ClientToken clientToken = new ClientToken(client.clientId, client.resourceIds as Set<String>,
                    client.clientSecret, client.scope as Set<String>, client.authorizedGrantTypes)
            final OAuth2Authentication authentication = new OAuth2Authentication(clientToken,
                    authentication(params))
            tokenServices.createAccessToken(authentication)
        }
        forward(action: "show", id: params.id)
    }

    def update = {
        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            forward(action: "list")
        } else if (!ldapUserDetailsManager.manages(userInstance, params.na)) {
            flash.message = "Cannot set this ftp account; as it does not belong to your naming authority."
            render(view: "edit", model: [userInstance: userInstance])
        }
        else if (params.confirmpassword && params.confirmpassword != params.password) {
            flash.message = "Passwords do not match"
            render(view: "edit", model: [userInstance: userInstance])
        }
        else {
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
        }
        else
        if (userInstance.id == springSecurityService.principal.username) {
            render(message(code: "delete.self"))
        }
        else
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

    /**
     * authentication
     *
     * Create an UsernamePasswordAuthenticationToken for the oauth authentication provider.
     * See config grails.plugins.springsecurity.controllerAnnotations.staticRules:
     * ROLE_OR_USER will allow access to the oauth controller
     * ROLE_OR_USER_[na] will allow access to the resource
     *
     * @param na
     * @return
     */
    private UsernamePasswordAuthenticationToken authentication(def params) {
        new UsernamePasswordAuthenticationToken(
                params.id,
                UUID.randomUUID().toString(),
                [
                        new GrantedAuthorityImpl('ROLE_OR_USER'),
                        new GrantedAuthorityImpl('ROLE_OR_USER_' + params.na)]
        )
    }
}