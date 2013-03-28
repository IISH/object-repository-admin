package org.objectrepository.security

import grails.plugins.springsecurity.Secured
import org.apache.commons.lang.RandomStringUtils

import org.objectrepository.ai.ldap.LdapUser
import org.objectrepository.ai.ldap.LdapUser.Essence
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.ClientToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl

/**
 * UserController
 *
 * We will use LDAP for authentication only. No synchronizations. Hence authorization without an existing local
 * user account will be impossible and treated as anonymous. The latter is determined by a local ROLE_USER.
 *
 * However, when persisting users in the local database; an update into LDAP will take place to ensure the Gid\home directory settings
 * that is used as a ftp account elsewhere.
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class UserController extends NamingAuthorityInterceptor {

    def springSecurityService
    def ldapUserDetailsManager
    def tokenStore
    def tokenServices
    def clientDetailsService

    static allowedMethods = [save: "POST", update: "POST", changekey: "POST"]
    final static loginshell = "/bin/false"

    def index = {
        forward(action: "list", params: params)
    }

    def list = {

        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def userInstanceList = []
        ldapUserDetailsManager.findLdapGroupUsers('OR_FTP_' + params.na).each {
            String username = it.split(',')[0].split('=')[1]
            if (springSecurityService.principal.username != username)
                userInstanceList << ldapUserDetailsManager.loadUserByUsername(username)
        }
        [userInstanceList: userInstanceList, userInstanceTotal: userInstanceList.size()]
    }

    def create = {
        def userInstance = new User()
        userInstance.enabled = true
        userInstance.na = params.na
        return [userInstance: userInstance]
    }

    def save = {

        // Set password when it was left empty
        if (params.password == "" && params.confirmpassword == "") {
            final String newPassword = RandomStringUtils.random(6, true, false)
            params.password = newPassword
            params.confirmpassword = newPassword
        }
        params.username = params.username.toLowerCase()

        def userInstance = User.findByUsername(params.username)
        if (userInstance) { // We are not a save.... but an update
            redirect(action: "update", id: userInstance.id)
            return
        }

        // confirm password check
        if (!params.skippassword) {
            if (params.password != params.confirmpassword) {
                flash.message = "Passwords do not match"
                render(view: "create", model: [userInstance: userInstance])
                return
            }
        }

        if (params.ldap && ldapUserDetailsManager?.userExists(params.username)) {
            flash.message = "User already has an account in LDAP. Choose a different name."
            render(view: "create", model: [userInstance: userInstance])
            return
        }

        userInstance = new User(params)
        def passw = userInstance.password
        userInstance.password = springSecurityService.encodePassword(passw)
        userInstance.na = params.na

        final def currentUser = User.findByUsername(springSecurityService.principal.username)
        userInstance.o = currentUser.o

        addRole(userInstance, params)

        // CPADMINs have an identical uid as the na
        long uidNumber = userInstance.na as Long
        userInstance.uidNumber = uidNumber

        if (userInstance.save()) {

            flash.message = "${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])}"

            def authority = "ROLE_OR_FTP_USER_" + params.na
            def userRole = Role.findByAuthority(authority) ?: new Role(authority: authority).save(failOnError: true)
            UserRole.create userInstance, userRole

            params.ldap = (params.ldap) ?: true
            if (params.ldap && ldapUserDetailsManager) {

                // Do not add if the user exists
                if (ldapUserDetailsManager.userExists(userInstance.username)) {
                    log.warn "Not adding user to LDAP as the account already is there. Ought not to have been called."
                } else {

                    final LdapUser.Essence person = new LdapUser.Essence();
                    def homeDirectory = (userRoles.contains('ROLE_CPADMIN')) ? grailsApplication.config.sa.path + "/" + userInstance.na : grailsApplication.config.sa.path + "/" + userInstance.na + "/" + userInstance.uidNumber
                    person.homeDirectory = homeDirectory
                    person.uidNumber = uidNumber
                    person.gidNumber = userInstance.na as Long
                    person.ou = userInstance.na as String
                    person.o = userInstance.o
                    person.username = userInstance.username
                    person.loginshell = loginshell
                    person.mail = userInstance.mail
                    person.username = userInstance.username
                    final boolean enabled = params.enabled ?: false
                    person.enabled = enabled
                    person.password = toggleEnable(passw, userInstance.enabled)
                    if (params.skippassword) userInstance.password = null
                    person.sn = userInstance.username
                    person.cn = [userInstance.username]
                    person.dn = ldapUserDetailsManager.usernameMapper.buildDn(userInstance.username)
                    ldapUserDetailsManager.createUser(person.createUserDetails())
                }
            }

            // send mail to new user
            def serverURL = grailsApplication.config.grails.serverURL
            final String mailFrom = grailsApplication.config.mail.from
            final String sftpServer = grailsApplication.config.mail.sftpServer

            if (params.sendmail) {
                sendMail {
                    to userInstance.mail
                    from mailFrom
                    subject message(code: "mail.user.created.subject")
                    body message(code: "mail.user.created.body", args: [currentUser.username, serverURL, sftpServer, userInstance.username, passw])
                }
            }

            redirect(action: "show", id: userInstance.id)
        }
        else {
            render(view: "create", model: [userInstance: userInstance])
        }

    }

    def show = {
        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
            return
        }

        def token = tokenStore.selectKeys(params.id)
        if (!token) {
            def client = clientDetailsService.clientDetailsStore.get("clientId")
            ClientToken clientToken = new ClientToken(client.clientId, client.resourceIds as Set<String>,
                    client.clientSecret, client.scope as Set<String>, client.authorizedGrantTypes)
            final OAuth2Authentication authentication = new OAuth2Authentication(clientToken,
                    new UsernamePasswordAuthenticationToken(
                            params.id,
                            UUID.randomUUID().toString(),
                            [
                                    new GrantedAuthorityImpl('ROLE_OR_USER'),
                                    new GrantedAuthorityImpl('ROLE_OR_USER_' + params.na)]
                    ))
            token = tokenServices.createAccessToken(authentication)
        }
        [userInstance: userInstance, token: token]
    }

    def edit = {
        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
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
                    new UsernamePasswordAuthenticationToken(
                            params.id,
                            UUID.randomUUID().toString(),
                            [
                                    new GrantedAuthorityImpl('ROLE_OR_USER'),
                                    new GrantedAuthorityImpl('ROLE_OR_USER_' + params.na)]
                    ))
            tokenServices.createAccessToken(authentication)
        }
        forward(action: "show", id: params.id)
    }

    def update = {
        def userInstance = ldapUserDetailsManager.loadUserByUsername(params.id)

        if (params.confirmpassword && params.confirmpassword != params.password) {
            flash.message = "Passwords do not match"
            render(view: "edit", model: [userInstance: userInstance])
        }
        else if (userInstance) {
            boolean passwordAltered = (params.confirmpassword != "" && params.password != userInstance.password)
            def passwd = (passwordAltered) ? springSecurityService.encodePassword(params.password) : userInstance.password
            params.password = toggleEnable(passwd, params.enabled)
            params.uid = userInstance.uid
            updateUser(params)
            flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            forward(action: "show", id: userInstance.id)
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            forward(action: "list")
        }
    }

    protected def toggleEnable(String password, boolean enable) {
        char trailer = password[0]
        if (enable && trailer == '!') {
            return password.substring(1)
        } else if (!enable && trailer != '!') {
            return "!" + password
        }
        password
    }

/**
 * updateUser
 *
 * Update the user in LDAP.
 *
 * @param userInstance
 */
    protected void updateUser(def params) {
        Essence person = new Essence();
        person.password = params.password
        person.homeDirectory = grailsApplication.config.sa.path + "/" + params.na + "/" + params.uid
        person.gidNumber = params.na
        person.username = params.id
        person.uidNumber = params.uid
        person.loginshell = loginshell
        person.mail = params.mail
        person.enabled = params.enabled
        person.sn = params.id
        person.cn = [params.id]
        person.dn = ldapUserDetailsManager.usernameMapper.buildDn(params.id)
        ldapUserDetailsManager.updateUser(person.createUserDetails())
    }

    def delete = {
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
        }

        if (userInstance.username == springSecurityService.principal.username) {
            render(message(code: "delete.self"))
            return
        }

        try {
            // manually remove User, TODO: wait for release of MongoDB 1.0.0.RC3 for cascading deletes
            UserRole.removeAll(userInstance)
            userInstance.delete()

            if (ldapUserDetailsManager) {
                // We should not remove users from LDAP. Only remove their bash to prevent ftp logins elsewhere?
                ldapUserDetailsManager.deleteUser(userInstance.username)
            }

            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])}"
            redirect(action: "list")
        }
        catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn e.message
            flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])}"
            redirect(action: "show", id: params.id)
        }
    }
}