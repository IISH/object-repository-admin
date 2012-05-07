package org.objectrepository.administration

import grails.plugins.springsecurity.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class DashboardController {

    def springSecurityService

    def index = {
    }
}
