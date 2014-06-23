package org.objectrepository.security

import org.apache.commons.lang.RandomStringUtils
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class UserController extends InterceptorValidation {

    static allowedMethods = [save: 'POST', update: 'POST']

    final static fixedPolicies = ['administration', 'all']

    def springSecurityService
    def gridFSService
    def oauth2Service

    def index() {
        forward(action: 'list', params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        final list = User.findAllByNa(params.na, params)
        respond list, model: [userInstanceList: list, userInstanceTotal: list.size()]
    }

    def create() {
        final userInstance = new User(na: params.na)
        respond userInstance, model: [userInstance: userInstance, policyList: _policyList()]
    }

    def save(User userInstance) {

        // Set password when it was left empty
        def newPassword
        if (!userInstance.password && !params.confirmpassword) {
            newPassword = RandomStringUtils.random(6, true, false)
            userInstance.password = newPassword
            userInstance.confirmpassword = newPassword
        } else
            newPassword = userInstance.password

        // confirm password check
        switch (request.format) {
            case 'html':
            case 'form':
                if (!params.skippassword) {
                    if (userInstance.password != params.confirmpassword) {
                        flash.message = 'Passwords do not match'
                        return render(view: 'create', status: HttpStatus.BAD_REQUEST.value(), model: [userInstance: params])
                    }
                }
                break
        }

        userInstance.password = springSecurityService.encodePassword(userInstance.password, UUID.randomUUID().encodeAsMD5Bytes())

        switch (status(userInstance)) {
            case HttpStatus.OK:
                userInstance.save flush: true
                def policyList = _policyList()
                userInstance.replaceKey = roles(userInstance, params.dissemination.findAll {
                    it.value == 'on' && (it.key in fixedPolicies || it.key in policyList)
                }.collect { it.key })
                def token = oauth2Service.updateKey(userInstance)

                def code = ('ROLE_OR_POLICY_administration' in userInstance.authorities*.authority) ? 'administration' : 'dissemination'
                if (params.sendmail) {
                    sendMail {
                        to params.mail
                        from grailsApplication.config.mail.from
                        subject message(code: 'mail.user.created.subject.' + code)
                        body message(code: 'mail.user.created.' + code, args: [grailsApplication.config.grails.serverURL, grailsApplication.config.ftp.host, userInstance.username, newPassword, token.value])
                    }
                }

                flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
                respond(userInstance, view: 'show', model: [userInstance: userInstance, policyList: _policyList(), token: token])
                break

            case HttpStatus.BAD_REQUEST:
                respond(userInstance, view: 'create', model: [userInstance: userInstance, policyList: _policyList()])
                break
        }
    }

    def show(User userInstance) {

        switch (status(userInstance)) {
            case HttpStatus.OK:
                def token = oauth2Service.selectKeys(userInstance.username)
                userInstance.replaceKey = (token)
                if (!token) token = oauth2Service.updateKey(userInstance)
                respond userInstance, model: [userInstance: userInstance, policyList: _policyList(), token: token]
                break

            case HttpStatus.BAD_REQUEST:
                flash.message = message(code: 'default.validation.message', args: [message(code: 'user.label', default: 'User'), params.id])
                respond(userInstance, forward: 'list')
                break
        }
    }

    def edit(User userInstance) {

        switch (status(userInstance)) {
            case HttpStatus.OK:
                respond userInstance, model: [userInstance: userInstance, policyList: _policyList()]
                break

            case HttpStatus.BAD_REQUEST:
                flash.message = message(code: 'default.validation.message', args: [message(code: 'user.label', default: 'User'), params.id])
                respond userInstance, forward(action: 'list')
                break
        }
    }

    def update(User userInstance) {

        def policyList = _policyList()

        switch (status(userInstance)) {
            case HttpStatus.OK:
                userInstance.save flush: true

                userInstance.replaceKey = roles(userInstance, params.dissemination.findAll {
                    it.value == 'on' && (it.key in fixedPolicies || it.key in policyList)
                }.collect { it.key })

                flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
                params.action = 'show'
                respond(userInstance, view: 'show', model: [userInstance: userInstance, token: oauth2Service.updateKey(userInstance), policyList: _policyList()])
                break

            case HttpStatus.BAD_REQUEST:
                flash.message = message(code: 'default.validation.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
                respond userInstance, view: 'edit', model: [userInstance: userInstance, policyList: _policyList()]
                break
        }
    }

    def delete(User userInstance) {

        switch (status(userInstance)) {
            case HttpStatus.OK:
                try {
                    userInstance.authorities?.each {
                        UserRole.remove(userInstance, it, true)
                    }
                    userInstance.delete flush: true
                    oauth2Service.removeToken(oauth2Service.selectKeys(userInstance.username))
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])

                    switch (request.format) {
                        case 'html':
                        case 'form':
                            forward(action: 'list')
                            break
                        default:
                            respond userInstance
                            break
                    }
                }
                catch (DataIntegrityViolationException e) {
                    log.warn e
                    flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])
                    switch (request.format) {
                        case 'html':
                        case 'form':
                            forward(action: 'show', id: userInstance.id)
                            break
                        default:
                            respond userInstance
                            break
                    }
                }
                break

            case HttpStatus.BAD_REQUEST:
                flash.message = message(code: 'default.validation.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
                respond(userInstance, forward(action: 'show', id: userInstance.id))
                break

        }
    }

    /**
     * roles
     *
     * Removes all authorities and resets those with the given parameters.
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
            flash.message = message(code: 'default.validation.message', args: [message(code: 'user.label', default: 'User'), id])
            return forward(action: 'list')
        }

        userInstance.replaceKey = true
        oauth2Service.updateKey(userInstance)
        forward(action: 'show', id: params.id)
    }

    def _policyList() {
        fixedPolicies + Policy.findAllByNa(params.na).access
    }
}
