package org.objectrepository.security

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.access.annotation.Secured

/**
 *  UserResourceController
 *
 *  There is no domain class for UserResource. Rather it is an embedded part of the User domain class
 *  The reference to the User instance will be uid
 *
 *  We will translate the embedded class into a list here for our view.
 */
@Secured(['ROLE_OR_USER'])
class UserResourceController extends NamingAuthorityInterceptor {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    def springSecurityService

    /**
     * List all pid values.... no limits
     * @param id user id
     * @return
     */
    def list(Long id) {
        final User userInstance = User.findByIdAndNa(id, params.na)
        [userResourceInstanceList: userInstance.resources, userResourceInstanceTotal: userInstance.resources.size()]
    }

    def create() {
        [userResourceInstance: new UserResource(params)]
    }

    def save() {
        def userResourceInstance = new UserResource(params)
        if (!userResourceInstance.save(flush: true)) {
            render(view: "create", model: [userResourceInstance: userResourceInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'userResource.label', default: 'UserResource'), userResourceInstance.id])
        redirect(action: "show", id: userResourceInstance.id)
    }

    def show(Long id) {
        def userResourceInstance = UserResource.get(id)
        if (!userResourceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userResource.label', default: 'UserResource'), id])
            redirect(action: "list")
            return
        }

        [userResourceInstance: userResourceInstance]
    }

    def edit(Long id) {
        def userResourceInstance = UserResource.get(id)
        if (!userResourceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userResource.label', default: 'UserResource'), id])
            redirect(action: "list")
            return
        }

        [userResourceInstance: userResourceInstance]
    }

    def update(Long id, Long version) {
        def userResourceInstance = UserResource.get(id)
        if (!userResourceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userResource.label', default: 'UserResource'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (userResourceInstance.version > version) {
                userResourceInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'userResource.label', default: 'UserResource')] as Object[],
                        "Another user has updated this UserResource while you were editing")
                render(view: "edit", model: [userResourceInstance: userResourceInstance])
                return
            }
        }

        userResourceInstance.properties = params

        if (!userResourceInstance.save(flush: true)) {
            render(view: "edit", model: [userResourceInstance: userResourceInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'userResource.label', default: 'UserResource'), userResourceInstance.id])
        redirect(action: "show", id: userResourceInstance.id)
    }

    def delete(Long id) {
        def userResourceInstance = UserResource.get(id)
        if (!userResourceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'userResource.label', default: 'UserResource'), id])
            redirect(action: "list")
            return
        }

        try {
            userResourceInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'userResource.label', default: 'UserResource'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'userResource.label', default: 'UserResource'), id])
            redirect(action: "show", id: id)
        }
    }
}
