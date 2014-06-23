package org.objectrepository.security

import org.springframework.security.access.annotation.Secured


@Secured(['IS_AUTHENTICATED_FULLY'])
class ResourceController {

    def springSecurityService

    def index() {
        forward(action: "list", params: params)
    }

    /**
     * list
     *
     * Retrieves the user by the principal that was stored in the OAUTH2 authentication wrapper.
     *
     * @return A list of all resources the user can access.
     */
    def list() {

        final User userInstance = User.findByUsername(springSecurityService.principal)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User')])
            return redirect(url: '/login/c403.gsp')
        }
        respond userInstance, model:[userInstance: userInstance, userResourceInstanceList: userInstance.resources, userResourceInstanceTotal: userInstance.resources.size(),
                ftp: [ host:grailsApplication.config.ftp.host ]]
    }
}
