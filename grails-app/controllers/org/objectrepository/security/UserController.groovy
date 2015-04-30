package org.objectrepository.security

import grails.plugin.springsecurity.oauthprovider.GormTokenStoreService
import grails.plugin.springsecurity.oauthprovider.provider.GrailsOAuth2RequestFactory
import org.apache.commons.lang.RandomStringUtils
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2RefreshToken
import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.OAuth2Authentication

@Secured(['ROLE_OR_USER'])
class UserController extends InterceptorValidation {

    static allowedMethods = [save: 'POST', update: 'POST']

    final static fixedPolicies = ['administration', 'all']
    final static CLIENT_ID = 'client_or'

    def springSecurityService
    def gridFSService
    def GormTokenStoreService gormTokenStoreService
    def GrailsOAuth2RequestFactory oauth2RequestFactory

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
        def newPassword = params.password
        if (!newPassword)
            newPassword = userInstance.password = params.confirmpassword = RandomStringUtils.random(6, true, false)

        // confirm password check
        switch (request.format) {
            case 'html':
            case 'form':
                if (userInstance.password != params.confirmpassword) {
                    flash.message = 'Passwords do not match'
                    return render(view: 'create', status: HttpStatus.BAD_REQUEST.value(), model: [userInstance: params])
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
                respond(userInstance, view: 'show', model: [userInstance: userInstance, policyList: _policyList(), token: getBearer(userInstance)])
                break

            case HttpStatus.BAD_REQUEST:
                respond(userInstance, view: 'create', model: [userInstance: userInstance, policyList: _policyList()])
                break
        }
    }

    def show(User userInstance) {

        switch (status(userInstance)) {
            case HttpStatus.OK:
                respond userInstance, model: [userInstance: userInstance, policyList: _policyList(), token: getBearer(userInstance)]
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

                // We only need a new key when the authorities have changed.
                userInstance.replaceKey = roles(userInstance, params.dissemination.findAll {
                    it.value == 'on' && (it.key in fixedPolicies || it.key in policyList)
                }.collect { it.key })

                flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
                params.action = 'show'
                respond(userInstance, view: 'show', model: [userInstance: userInstance, token: updateKey(userInstance), policyList: _policyList()])
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
                    removeToken(userInstance)
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

    def _policyList() {
        fixedPolicies + Policy.findAllByNa(params.na).access
    }

    def updatekey(Long id) {

        def userInstance = User.findByIdAndNa(id, params.na)
        if (userInstance) {
            updateKey(userInstance)
            forward(action: 'show', id: params.id)
        } else {
            flash.message = message(code: 'default.validation.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: 'list')
        }
    }

    def updateKey(User user) {
        if (user.replaceKey)
            removeToken(user)
        getBearer(user)
    }

    /**
     * getBearer
     * Retrieved the current bearer token from the token store.
     * Create a token if it did not exist.
     *
     * @param user the user associated with the token
     * @return A token
     */
    private OAuth2AccessToken getBearer(User user) {
        (gormTokenStoreService.findTokensByClientIdAndUserName(CLIENT_ID, user.username)?.find {
            it.tokenType == 'bearer'
        }) ?: saveBearer(user)
    }

    /**
     * saveBearer
     *
     * Create an OAUTH2 access token. We hardwire it here, because we do not allow for user account UI logins.
     *
     * @param user
     * @return
     */
    private OAuth2AccessToken saveBearer(User user, expiration = new Date().plus(365)) {

        def scope = ['read']
        final authorizationRequest = new AuthorizationRequest(CLIENT_ID, scope)
        final storedRequest = oauth2RequestFactory.createOAuth2Request(authorizationRequest)
        final authentication = new OAuth2Authentication(storedRequest,
                authentication(user))

        final OAuth2RefreshToken refreshToken = new DefaultExpiringOAuth2RefreshToken(UUID.randomUUID().toString(), expiration)
        gormTokenStoreService.storeRefreshToken(refreshToken, authentication)

        final OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(UUID.randomUUID().toString())
        accessToken.refreshToken = refreshToken
        accessToken.tokenType = 'bearer'
        accessToken.expiration = expiration
        accessToken.scope = scope
        gormTokenStoreService.storeAccessToken(accessToken, authentication)
        accessToken
    }

    private removeToken(User user) {
        gormTokenStoreService.findTokensByClientIdAndUserName(CLIENT_ID, user.username)?.each {
            gormTokenStoreService.removeRefreshToken(it.refreshToken)
            gormTokenStoreService.removeAccessToken(it)
        }
    }

    /**
     * authentication
     *
     * Create an UsernamePasswordAuthenticationToken for the oauth authentication provider.
     *
     * @param id Identifier of the user
     * @param authorities Roles of the user
     * @return
     */
    static def authentication(User user) {

        new UsernamePasswordAuthenticationToken(
                user.username,
                user.password,
                user.authorities.collect {
                    new SimpleGrantedAuthority(it.authority)
                }
        )
    }

}