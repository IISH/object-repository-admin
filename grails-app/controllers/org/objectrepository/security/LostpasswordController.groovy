package org.objectrepository.security

import grails.plugins.springsecurity.Secured
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.objectrepository.ai.ldap.LdapUser.Essence

@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class LostpasswordController {

    def springSecurityService
    def ldapUserDetailsManager
    def index = { }

    /**
     * Sends a mail with a link to first password. Generates a random password and validation code.
     *
     * @param mailThe mail of the user whose password to first.
     * @return returns a message whether user was found
     */
    def newpass = {

        if (params.mail) {
            User user = User.findByMail(params.mail)

            if (user && user.enabled) {
                user.newpassword = (user.newpassword == null) ?
                    RandomStringUtils.random(6, true, false) : user.newpassword;
                user.verification = (user.verification == null) ?
                    RandomStringUtils.random(25, true, true) : user.verification;
                user.expiration = new Date().getTime() + 1000 * 60 * 60 * 24

                final String serverURL = grailsApplication.config.grails.serverURL
                if (!user.hasErrors() && user.save(flush: true)) {
                    def verificationUrl = serverURL + this.controllerUri + "/verify?v=" + user.verification
                    def config = ConfigurationHolder.config
                    final String strFrom = config.mail.from
                    sendMail {
                        to params.mail
                        from strFrom
                        subject message(code: "mail.forgot.subject")
                        body message(code: 'mail.forgot.body', args: [user.username, user.newpassword, verificationUrl])
                    }
                }
                flash.message = message(code: "lostpassword.mail.newpassword", args: [params.mail])
                redirect(action: sent)
            }
            else {
                flash.message = message(code: "lostpassword.invalid.user")
                redirect(action: index)
            }
        }
        else {
            flash.message = message(code: "lostpassword.invalid.mail")
            redirect(action: index)
        }
    }

    def sent = {
    }

    /**
     * Reset user's password after a first list has been followed and sends mail with new password.
     *
     * The action takes two parameters
     * @param user The username of the user whose password to first.
     * @param verification A code which must correspond to the verification property which was generated when a password first was requested.
     * @return returns a message whether first was (un)successful
     */
    def verify = {
        def msg = "${message(code: 'verification.invalid.code')}"

        if (params.v == null || params.v == "") {
            msg = "${message(code: 'verification.empty.code')}"
        }
        else {
            def User user = User.findByVerification(params.v)
            final long t_now = new Date().getTime()
            if (user && user.enabled && user.expiration > t_now && params.v == user.verification) {
                def password = user.newpassword
                user.password = (ldapUserDetailsManager) ? RandomStringUtils.random(8, true, true) : springSecurityService.encodePassword(password);
                user.verification = null
                user.newpassword = null
                if (!user.hasErrors() && user.save(flush: true)) {
                    if (ldapUserDetailsManager) updateUser(user, password)
                    msg = "${message(code: 'verification.accepted.code')}"
                }
                else {
                    msg = user.errors.globalError.toString()
                }
            }
        }
        return [userInstance: msg]
    }

    protected void updateUser(org.objectrepository.security.User userInstance, String password) {
        Essence person = new Essence();
        person.password = password
        def cprole = UserRole.findByRoleAndUser(new Role(authority: "ROLE_CPADMIN"), userInstance)
        def homeDirectory = (cprole) ? grailsApplication.config.sa.path + "/" + userInstance.na : grailsApplication.config.sa.path + "/" + userInstance.na + "/" + userInstance.getUidNumber()
        person.homeDirectory = homeDirectory
        person.gidNumber = userInstance.na as Long
        person.ou = userInstance.na
        person.o = userInstance.o
        person.username = userInstance.username
        person.uidNumber = userInstance.uidNumber
        person.loginshell = "/bin/sh"
        person.mail = userInstance.mail
        person.enabled = userInstance.enabled
        person.sn = userInstance.username
        person.cn = [userInstance.username]
        person.dn = ldapUserDetailsManager.usernameMapper.buildDn(userInstance.username)
        ldapUserDetailsManager.updateUser(person.createUserDetails())
    }
}