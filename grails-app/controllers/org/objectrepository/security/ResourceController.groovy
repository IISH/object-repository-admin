package org.objectrepository.security

import grails.plugins.springsecurity.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class ResourceController {

    def springSecurityService

    def index = {
        forward(action: "list", params: params)
    }

    /**
     * list
     *
     * @return A list of all resources the user can access.
     */
    def list() {

        final User userInstance = User.findByUsername(springSecurityService.principal)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User')])
            return redirect(url: '/login/c403.gsp')
        }
        [userInstance: userInstance, userResourceInstanceList: userInstance.resources, userResourceInstanceTotal: userInstance.resources.size(),
                ftp: [ host:grailsApplication.config.ftp.host ]]
    }
}
