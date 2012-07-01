package org.objectrepository.administration

import grails.plugins.springsecurity.Secured
import org.objectrepository.security.User
import org.apache.commons.lang.RandomStringUtils
import org.objectrepository.security.UserRole
import org.objectrepository.security.Role
import org.objectrepository.instruction.Profile
import org.objectrepository.util.OrUtil

@Secured(['IS_AUTHENTICATED_FULLY'])
class DashboardController {

    def springSecurityService
    def ldapUserDetailsManager
    def gridFSService

    /**
     * See if we need to create a user
     */
    def index = {
        log.info "Check if adding an account is needed..."
        final String na = springSecurityService.principal.na
        if (na && ldapUserDetailsManager) {
            log.info "We are a ldap user "
            def currentUser = User.findByUsername(springSecurityService.principal.username)
            if (!currentUser) {
                log.info "No user in db"
                currentUser = new User(na: na,
                        username: springSecurityService.principal.username,
                        uidNumber: springSecurityService.principal.uidNumber,
                        password: RandomStringUtils.random(6, true, false),
                        o: "not available",
                        mail: springSecurityService.principal.mail
                )
                if (currentUser.save(flush: true)) {
                    def role = (Role.findByAuthority("ROLE_CPADMIN")) ?: new Role(authority: "ROLE_CPADMIN").save(failOnError: true)
                    UserRole.create currentUser, role
                } else {
                    flash.message = "Could not add user."
                    return
                }

                log.info "Policies and Profile"
                OrUtil.availablePolicies(na, grailsApplication.config.accessMatrix)
                if (!Profile.findByNa(na)) new Profile(na: na).save(failOnError: true)

                springSecurityService.reauthenticate(springSecurityService.principal.username)
            }
        }
    }
}
