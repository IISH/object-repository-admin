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
        final list = User.findAllByNa(params.na, params)
        [userInstanceList: list, userInstanceTotal: User.countByNa(params.na)]
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
        def newPassword
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
        def token = ldapUserDetailsManager.updateKey(userInstance)


        def code = ('ROLE_OR_POLICY_administration' in userInstance.authorities*.authority) ? 'administration' : 'dissemination'
        if (params.sendmail) {
            sendMail {
                to params.mail
                from grailsApplication.config.mail.from
                subject message(code: "mail.user.created.subject." + code)
                body message(code: "mail.user.created." + code, args: [grailsApplication.config.grails.serverURL, grailsApplication.config.ftp.host, userInstance.username, newPassword, token.value])
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

        flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        params.action = 'show'
        render(view: "show", model: [userInstance: userInstance, token: ldapUserDetailsManager.updateKey(userInstance), policyList: policyList])
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

        dissemination.removeAll {
            it.startsWith('ROLE_OR_POLICY_') || it.startsWith('ROLE_OR_DISSEMINATION_')
        }

        List r = (fixedPolicies[0] in dissemination) ? ['ROLE_OR_USER', 'ROLE_OR_USER_' + userInstance.na] : []
        r += dissemination.collect {
            'ROLE_OR_POLICY_' + it
        } + 'ROLE_OR_DISSEMINATION_'.concat(userInstance.na)

        def currentAuthorities = userInstance.authorities?.collect {
            it.authority
        }

        def removals = currentAuthorities.minus(r)
        removals.each { authority ->
            UserRole.remove(userInstance, userInstance.authorities.find {
                it.authority == authority
            })
        }

        def additions = r.minus(currentAuthorities)
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
