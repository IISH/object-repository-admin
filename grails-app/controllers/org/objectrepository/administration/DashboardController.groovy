package org.objectrepository.administration

import grails.plugins.springsecurity.Secured
import org.objectrepository.security.User
import org.apache.commons.lang.RandomStringUtils
import org.objectrepository.security.UserRole
import org.objectrepository.security.Role
import org.objectrepository.instruction.Profile
import org.objectrepository.util.OrUtil

@Secured(['ROLE_ADMIN', 'ROLE_CPADMIN'])
class DashboardController {

    def springSecurityService
    def ldapUserDetailsManager
    def gridFSService
    def statisticsService

    /**
     * See if we need to create a user
     */
    def index = {
        log.info "Checking if adding an account is needed..."
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
                springSecurityService.reauthenticate(springSecurityService.principal.username)
            }
            log.info "Policies and Profile"
            OrUtil.availablePolicies(na, grailsApplication.config.accessMatrix)
            if (!Profile.findByNa(na)) new Profile(na: na).save(failOnError: true)
        }

        final interval = (params.interval) ?: 'year'
        [storage: statisticsService.getStorage(na, interval), siteusage: statisticsService.getSiteusage(na, interval), tasks: null]
    }
}
