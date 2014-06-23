package org.objectrepository.administration

import grails.converters.JSON
import grails.converters.XML
import org.objectrepository.instruction.Profile
import org.objectrepository.security.InterceptorValidation
import org.objectrepository.util.OrUtil
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class DashboardController extends InterceptorValidation {

    def springSecurityService
    def ldapUserDetailsManager
    def gridFSService
    def statisticsService

    /**
     * See if we need to create a user
     */
    def index() {
        log.info "Checking if adding an account is needed..."
        if (params.na && ldapUserDetailsManager) {
            log.info "We are a ldap user "
            log.info "Policies and Profile"
            OrUtil.availablePolicies(params.na, grailsApplication.config.accessMatrix)
            if (!Profile.findByNa(params.na)) new Profile(na: params.na).save(failOnError: true)
        }

        final interval = (params.interval) ?: 'all'
        def ret = [storage: statisticsService.getStorage(params.na, interval), tasks: null]
        if (grailsApplication.config.siteusage)
            ret << [siteusage: statisticsService.getSiteusage(params.na, interval)]

        switch (request.format) {
            case 'json':
                if (params.callback)
                    render(text: "${params.callback}(${ret as JSON})", contentType: 'application/json')
                else
                    render ret as JSON
                break
            case 'xml':
                render ret as XML
                break
            case 'html':
            case 'format':
            default:
                ret
                break;

        }
    }

}


