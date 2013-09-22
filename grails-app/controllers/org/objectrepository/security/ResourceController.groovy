package org.objectrepository.security

import org.springframework.security.access.annotation.Secured

//@Secured(['ROLE_OR_FTPUSER'])
class ResourceController {

    def springSecurityService

    /**
     * beforeInterceptor
     *
     * Anyone with the correct NA may see this controller's actions.
     */
    def beforeInterceptor = {
        def role = 'ROLE_OR_FTPUSER_' + params.na
        if (springSecurityService.isLoggedIn() && (role in springSecurityService.authentication.authorities*.authority)) {
            true
        } else {
            redirect(controller: 'login', action: 'c403')
            false
        }
    }

    def index = {
        forward(action: "list", params: params)
    }

    def list(String na) {
        final User userInstance = User.findByUsername(na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), na])
            redirect(url: '/login/c403.gsp')
            return
        }
        render(view: '../userResource/list.gsp', model: [userInstance: userInstance, userResourceInstanceList: userInstance.resources, userResourceInstanceTotal: userInstance.resources.size()])
    }
}
