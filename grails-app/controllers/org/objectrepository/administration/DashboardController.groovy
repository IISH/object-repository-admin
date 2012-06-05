package org.objectrepository.administration

import grails.plugins.springsecurity.Secured
import org.objectrepository.security.User
import org.apache.commons.lang.RandomStringUtils
import org.objectrepository.security.UserRole
import org.objectrepository.security.Role

@Secured(['IS_AUTHENTICATED_FULLY'])
class DashboardController {

    def springSecurityService
    def ldapUserDetailsManager

    /**
     * See if we need to create a user
     */
    def index = {
        if (springSecurityService.principal.na && ldapUserDetailsManager) {
            def currentUser = User.findByUsername(springSecurityService.principal.username)
            if (!currentUser) {
                currentUser = new User(na: springSecurityService.principal.na,
                        username: springSecurityService.principal.username,
                        uidNumber: springSecurityService.principal.uidNumber,
                        password: RandomStringUtils.random(6, true, false),
                        o: "not available",
                        mail: springSecurityService.principal.mail
                )
                if (currentUser.save(flush: true)) {
                    UserRole.create currentUser, Role.findByAuthority("ROLE_CPADMIN")
                } else flash.message = "Could not add user."
            }
        }
    }
}
