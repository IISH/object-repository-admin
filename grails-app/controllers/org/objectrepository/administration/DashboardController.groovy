package org.objectrepository.administration

import grails.plugins.springsecurity.Secured
import org.objectrepository.security.User
import org.apache.commons.lang.RandomStringUtils
import org.objectrepository.security.UserRole
import org.objectrepository.security.Role
import org.objectrepository.instruction.Profile
import org.objectrepository.util.OrUtil

import org.objectrepository.security.NamingAuthorityInterceptor

@Secured(['IS_AUTHENTICATED_FULLY'])
class DashboardController extends NamingAuthorityInterceptor {

    def springSecurityService
    def ldapUserDetailsManager
    def gridFSService
    def statisticsService

    /**
     * See if we need to create a user
     */
    def index = {
        log.info "Checking if adding an account is needed..."
        if (params.na && ldapUserDetailsManager) {
            log.info "We are a ldap user "
            log.info "Policies and Profile"
            OrUtil.availablePolicies(params.na, grailsApplication.config.accessMatrix)
            if (!Profile.findByNa(params.na)) new Profile(na: params.na).save(failOnError: true)
        }

        final interval = (params.interval) ?: 'year'
        [storage: statisticsService.getStorage(params.na, interval), siteusage: statisticsService.getSiteusage(params.na, interval), tasks: null]
    }
}
