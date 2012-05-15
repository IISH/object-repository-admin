package org.objectrepository.security

import grails.plugins.springsecurity.Secured
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.objectrepository.ai.ldap.LdapUser
import org.objectrepository.ai.ldap.LdapUser.Essence
import org.springframework.security.oauth2.common.OAuth2AccessToken

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
@Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
class UserController {

    def springSecurityService
    def ldapUserDetailsManager
    def oauthTokenServices

    static allowedMethods = [save: "POST", update: "POST", changekey: "POST"]
    final static loginshell = "/bin/sh"

    def index = {

        forward(action: "list", params: params)
    }

    def list = {

        def currentUser = User.findByUsername(springSecurityService.principal.username)
        if (springSecurityService.hasRole('ROLE_CPUSER')) {
            redirect(action: "show", id: currentUser.id)
        } else {
            params.max = Math.min(params.max ? params.int('max') : 10, 100)
            def userList = springSecurityService.hasRole('ROLE_ADMIN') ? User.list(params) : User.findAllByNa(currentUser.na, params)  // Only show users belonging to the group of current cpadmin
            [currentUsername: currentUser.username, userInstanceList: userList, userInstanceTotal: userList.size()]
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
    def create = {
        def userInstance = new User()
        userInstance.enabled = true
        userInstance.na = springSecurityService.principal.na
        return [userInstance: userInstance]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
    def save = {

        // Set password when it was left empty
        if (params.password == "" && params.confirmpassword == "") {
            final String newPassword = RandomStringUtils.random(6, true, false)
            params.password = newPassword
            params.confirmpassword = newPassword
        }
        params.username = params.username.toLowerCase()

        if (springSecurityService.hasRole('ROLE_CPADMIN')) { // Make sure we do not set someone else's na here...
            params.na = springSecurityService.principal.na
        }

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
        long uidNumber = (params.role == 'ROLE_CPADMIN') ? userInstance.na as Long : User.list(max: 1, sort: 'uidNumber', order: 'desc').get(0).uidNumber + 1
        userInstance.uidNumber = uidNumber

        if (userInstance.save()) {

            flash.message = "${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])}"

            // add user role to new user. Only an ADMIN can set roles
            def userRoles = (params.role && springSecurityService.hasRole('ROLE_ADMIN')) ? [params.role] : ['ROLE_CPUSER']
            userRoles.each() { String authority ->
                def userRole = Role.findByAuthority(authority) ?: new Role(authority: authority).save(failOnError: true)
                UserRole.create userInstance, userRole
            }

            params.ldap = (params.ldap) ?: springSecurityService.hasRole('ROLE_CPADMIN')
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
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
            return
        }

        def currentUser = User.findByUsername(springSecurityService.principal.username)
        if (springSecurityService.hasRole('ROLE_CPUSER') && userInstance.username != springSecurityService.principal.username) {
            redirect(action: "show", id: currentUser.id)
            return
        }

        if (!springSecurityService.hasValidNa(userInstance.na)) {
            flash.message = "${message(code: 'user.default.unauthorized')}"
            redirect(action: "list")
            return
        }

        OAuth2AccessToken token = null
        if (springSecurityService.hasRole('ROLE_CPADMIN')) {
            token = oauthTokenServices.selectKeys(userInstance.username)
            if (!token) token = oauthTokenServices.createToken(springSecurityService.authentication)
        }
        [userInstance: userInstance, currentUsername: springSecurityService.principal.username, token: token]
    }

    def edit = {
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
            return
        }

        def currentUser = User.findByUsername(springSecurityService.principal.username)
        if (springSecurityService.hasRole('ROLE_CPUSER') && userInstance.id != currentUser.id) {
            flash.message = "${message(code: 'user.default.unauthorized')}"
            redirect(action: "show", id: currentUser.id)
            return
        }

        if (!springSecurityService.hasValidNa(userInstance.na)) {
            flash.message = "${message(code: 'user.default.unauthorized')}"
            redirect(action: "list")
            return
        }

        return [userInstance: userInstance, currentUsername: springSecurityService.principal.username]
    }

    @Secured(['ROLE_CPADMIN'])
    def updatekey = {

        def userInstance = User.get(params.id)
        if (!springSecurityService.hasValidNa(userInstance.na)) {
            flash.message = "${message(code: 'user.default.unauthorized')}"
            redirect(action: "list")
            return
        }
        OAuth2AccessToken token = oauthTokenServices.selectKeys(userInstance.username)
        if (!token) token = oauthTokenServices.createToken(springSecurityService.authentication)
        oauthTokenServices.recreateRefreshAccessToken(token.refreshToken.value)
        redirect(action: "show", id: params.id)
    }

    def update = {
        def userInstance = User.get(params.id)

        if (springSecurityService.hasRole('ROLE_CPUSER') && userInstance.username != springSecurityService.principal.username) {
            flash.message = "${message(code: 'user.default.unauthorized')}"
            redirect(action: "index")
        }

        if (!springSecurityService.hasValidNa(userInstance.na)) {   // Cannot add \ update a record from a user with another na
            flash.message = "${message(code: 'user.default.unauthorized')}"
            redirect(action: "list")
        }

        if (params.confirmpassword && params.confirmpassword != params.password) {
            flash.message = "Passwords do not match"
            render(view: "edit", model: [userInstance: userInstance])
            return;
        }

        if (userInstance) {
            boolean passwordAltered = (params.confirmpassword != "" && params.password != userInstance.password)
            userInstance.properties = params
            if (passwordAltered) {
                userInstance.password = springSecurityService.encodePassword(userInstance.password)
                userInstance.newpassword = null
                userInstance.verification = null
            }

            if (params.role) addRole(userInstance, params)

            if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
                def encpass = (ldapUserDetailsManager) ? ldapUserDetailsManager.loadUserByUsername(userInstance.username).password : springSecurityService.principal.password
                userInstance.enabled = params.enabled
                def passwd = (passwordAltered) ? params.password : encpass
                userInstance.password = toggleEnable(passwd, userInstance.enabled)
                if (ldapUserDetailsManager) updateUser(userInstance)
                userInstance.password = "*"
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])}"
                redirect(action: "show", id: userInstance.id)
            }
            else {
                render(view: "edit", model: [userInstance: userInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
        }
    }

    private def addRole(User userInstance, GrailsParameterMap params) {
        if (params.role == 'ROLE_ADMIN' && springSecurityService.hasRole('ROLE_CPADMIN')) {
            // Sneak injection... just do not act here.
            params.role = 'ROLE_CPUSER'
        }
        UserRole.removeAll(userInstance)
        UserRole.create userInstance, Role.findByAuthority(params.role)
    }

    protected toggleEnable(String password, boolean enable) {
        char trailer = password[0]
        if (enable && trailer == '!') {
            return password.substring(1)
        } else if (!enable && trailer != '!') {
            return "!" + password
        }
        password
    }

    protected void updateUser(org.objectrepository.security.User userInstance) {
        def currentUser = User.findByUsername(springSecurityService.principal.username)
        Essence person = new Essence();
        person.password = userInstance.password
        def cprole = UserRole.findByRoleAndUser(new Role(authority: "ROLE_CPADMIN"), userInstance)
        def homeDirectory = (cprole) ? grailsApplication.config.sa.path + "/" + userInstance.na : grailsApplication.config.sa.path + "/" + userInstance.na + "/" + userInstance.uidNumber
        person.homeDirectory = homeDirectory
        person.gidNumber = currentUser.na as Long
        person.ou = currentUser.na
        person.o = currentUser.o
        person.username = userInstance.username
        person.uidNumber = userInstance.uidNumber
        person.loginshell = loginshell
        person.mail = userInstance.mail
        person.enabled = userInstance.enabled
        person.sn = userInstance.username
        person.cn = [userInstance.username]
        person.dn = ldapUserDetailsManager.usernameMapper.buildDn(userInstance.username)
        ldapUserDetailsManager.updateUser(person.createUserDetails())
    }

    @Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
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

        if (!springSecurityService.hasValidNa(userInstance.na)) {
            flash.message = "${message(code: 'user.default.unauthorized')}"
            redirect(action: "list")
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