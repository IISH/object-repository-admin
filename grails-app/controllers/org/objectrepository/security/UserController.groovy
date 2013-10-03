package org.objectrepository.security

import org.apache.commons.lang.RandomStringUtils
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class UserController extends NamingAuthorityInterceptor {

    static allowedMethods = [save: "POST", update: "POST"]
    final static fixedPolicies = ['administration', 'all']

    def springSecurityService
    def gridFSService
    def ldapUserDetailsManager

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        final list = User.findAllByNa(params.na)
        [userInstanceList: list, userInstanceTotal: list.size()]
    }

    def create() {
        [userInstance: new User(params), policyList: fixedPolicies + Policy.findAllByNa(params.na).access]
    }

    def save() {

        switch (request.format) {
            case 'js':
            case 'json':
            case 'xml':
                return forward(controller: 'userPermission', action: 'save')
        }

        // Set password when it was left empty
        final def newPassword
        if (params.password == "" && params.confirmpassword == "") {
            newPassword = RandomStringUtils.random(6, true, false)
            params.password = newPassword
            params.confirmpassword = newPassword
        } else
            newPassword = params.password
        params.username = params.username.toLowerCase()

        if (User.findByUsername(params.username)) {
            flash.message = "Account already exists. Choose a different name."
            return render(view: "create", model: [userInstance: params, policyList: fixedPolicies + Policy.findAllByNa(params.na).access])
        }

        // confirm password check
        if (!params.skippassword) {
            if (params.password != params.confirmpassword) {
                flash.message = "Passwords do not match"
                return render(view: "create", model: [userInstance: params])
            }
        }
        params.password = springSecurityService.encodePassword(params.password, UUID.randomUUID().encodeAsMD5Bytes())

        def userInstance = new User(params)
        if (!userInstance.save(flush: true))
            return render(view: "create", model: [userInstance: userInstance, policyList: fixedPolicies + Policy.findAllByNa(params.na).access])

        def policyList = create().policyList
        userInstance.replaceKey = roles(userInstance, params.dissemination.findAll {
            it.value == 'on' && (it.key in fixedPolicies || it.key in policyList)
        }.collect { it.key })
        ldapUserDetailsManager.updateKey(userInstance)

        if (params.sendmail) {
            sendMail {
                to params.mail
                from grailsApplication.config.mail.from
                subject message(code: "mail.user.created.subject")
                body message(code: "mail.user.created." + userInstance.accessScope, args: [springSecurityService.principal.username,
                        grailsApplication.config.grails.serverURL,
                        grailsApplication.config.mail.sftpServer,
                        params.id,
                        newPassword])
            }
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        forward(action: "show", id: userInstance.id)
    }

    def show(Long id) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            return forward(action: "list")
        }

        def token = ldapUserDetailsManager.selectKeys(userInstance.username)
        userInstance.replaceKey = (token)
        if (!token) token = ldapUserDetailsManager.updateKey(userInstance)
        [userInstance: userInstance, policyList: fixedPolicies + Policy.findAllByNa(params.na).access, token: token]
    }

    def edit(Long id) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
        } else
            [userInstance: userInstance, policyList: fixedPolicies + Policy.findAllByNa(params.na).access]
    }

    def update(Long id, Long version) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            return forward(action: "list")
        } else if (version != null) {
            if (userInstance.version > version) {
                userInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'user.label', default: 'User')] as Object[],
                        "Another ftp has updated this User while you were editing")
                return render(view: "edit", model: [userInstance: userInstance])
            }
        }

        userInstance.properties = params
        if (!userInstance.save(flush: true)) {
            return render(view: "edit", model: [userInstance: userInstance])
        }

        def policyList = create().policyList
        userInstance.replaceKey = roles(userInstance, params.dissemination.findAll {
            it.value == 'on' && (it.key in fixedPolicies || it.key in policyList)
        }.collect { it.key })

        ldapUserDetailsManager.updateKey(userInstance)

        flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        forward(action: "show", id: userInstance.id)
    }

    /**
     * roles
     *
     * Removes all authorities and resets these with the given parameters.
     *
     * @param userInstance
     * @param dissemination
     * @return True if there was a change in the user's authority status
     */
    static boolean roles(def userInstance, def dissemination) {

        fixedPolicies.each { scope ->
            if (scope in dissemination)
                dissemination.removeAll { it != scope }
        }

        List roles = (fixedPolicies[0] in dissemination) ? ['ROLE_OR_USER', 'ROLE_OR_USER_' + userInstance.na] : []
        roles += dissemination.collect {
            'ROLE_OR_DISSEMINATION_' + it + '_' + userInstance.na
        }

        def currentAuthorities = userInstance.authorities?.collect {
            it.authority
        }

        def removals = currentAuthorities.minus(roles)
        removals.each { authority ->
            UserRole.remove(userInstance, userInstance.authorities.find {
                it.authority == authority
            })
        }

        def additions = roles.minus(currentAuthorities)
        additions.each {
            UserRole.create userInstance,
                    Role.findByAuthority(it) ?: new Role(authority: it).save(failOnError: true)
        }

        removals || additions
    }

    def updatekey(Long id) {

        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            return forward(action: "list")
        }

        userInstance.replaceKey = true
        ldapUserDetailsManager.updateKey(userInstance)
        forward(action: "show", id: params.id)
    }

    def delete(Long id) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            return forward(action: "list")
        }

        try {
            userInstance.authorities?.each {
                UserRole.remove(userInstance, it, true)
            }

            userInstance.delete(flush: true)
            ldapUserDetailsManager.removeToken(ldapUserDetailsManager.selectKeys(userInstance.username))
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "show", id: id)
        }
    }

}
