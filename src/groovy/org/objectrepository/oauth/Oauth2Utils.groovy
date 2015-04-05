package org.objectrepository.oauth
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.AuthorizationRequest
import org.springframework.security.oauth2.provider.OAuth2Authentication
/**
 *  Oauth2Service
 *
 *  Utility methods for non standardized OAUTH2 usage
 */
class Oauth2Service {

    def tokenServices
    def tokenStore
    def clientDetailsService

    def selectKeys(def username) {
        tokenStore.selectKeys(username)
    }

    def updateKey(def userInstance) {
        (userInstance.replaceKey) ? replaceKey(userInstance) : refreshKey(userInstance)
    }

    /***
     * replacekey
     *
     * Replaces the existing key with a new one
     *
     * @param userInstance
     */
    private def replaceKey(def userInstance) {
        tokenServices.createAccessToken(refresh(userInstance))
    }

    /**
     * refreshkey
     *
     * Refreshes the current key with new resources.
     * If the key does not exist, we create a new one
     *
     * @param userInstance
     */
    private def refreshKey(def userInstance) {
        final token = selectKeys(userInstance.username)
        if (token) {
            tokenStore.updateAuthentication(token, refresh(userInstance))
            token
        } else
            tokenServices.createAccessToken(refresh(userInstance))
    }

    private OAuth2Authentication refresh(def userInstance) {
        removeToken(selectKeys(userInstance.username))
        def client = clientDetailsService.clientDetailsStore.get("clientId")
        def ar = new AuthorizationRequest(client.clientId, client.scope)
        ar.approved = true
        new OAuth2Authentication(ar.createOAuth2Request(), authentication(userInstance, userInstance.authorities))
    }

    void removeToken(def token) {
        if (token) {
            tokenStore.removeAccessTokenUsingRefreshToken(token.refreshToken)
            tokenStore.removeRefreshToken(token.refreshToken)
        }
    }

    /**
     * authentication
     *
     * Create an UsernamePasswordAuthenticationToken for the oauth authentication provider.
     * See config grails.plugin.springsecurity.controllerAnnotations.staticRules:
     * ROLE_OR_USER will allow access to the oauth controller
     * ROLE_OR_USER_[na] will allow access to the resource
     *
     * @param id Identifier of the user
     * @param authorities Roles of the user
     * @return
     */
    static def authentication(def userInstance, def authorities) {

        new UsernamePasswordAuthenticationToken(
                userInstance.username,
                userInstance.password,
                authorities?.collect {
                    new SimpleGrantedAuthority(it.authority)
                }
        )
    }
}
