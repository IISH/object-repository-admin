package org.objectrepository.ldap

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.objectrepository.ai.ldap.LdapUser
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.ldap.LdapUsernameToDnMapper
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager
import org.springframework.security.oauth2.provider.ClientToken
import org.springframework.security.oauth2.provider.OAuth2Authentication

public class CustomLdapUserDetailsManager extends LdapUserDetailsManager {

    def _template
    def _usernameMapper
    def grailsApplication
    def springSecurityService
    def clientDetailsService
    def tokenStore
    def tokenServices
    final static loginshell = "/bin/sh"
    final static FTP_ROLE = "OR_SA_"

    public CustomLdapUserDetailsManager(ContextSource contextSource) {
        super(contextSource);
        _template = new LdapTemplate(contextSource)
    }

    public void setUsernameMapper(LdapUsernameToDnMapper usernameMapper) {
        super.setUsernameMapper(usernameMapper)
        _usernameMapper = usernameMapper;
    }

    def listLdapGroupUsers(String groupID) {
        DistinguishedName dn = new DistinguishedName(SpringSecurityUtils.securityConfig.ldap.authorities.groupSearchBase)
        dn.add(SpringSecurityUtils.securityConfig.ldap.groupAttribute, groupID)

        try {
            _template.lookup(dn,
                    new ContextMapper() {
                        public Object mapFromContext(Object ctx) {
                            DirContextAdapter adapter = (DirContextAdapter) ctx;
                            adapter.getStringAttributes("uniqueMember");
                        }
                    })
        } catch (javax.naming.NameNotFoundException e) {
            log.warn(e)
            log.warn("Ask the objectrepository administrator to set the ldap group role " + dn.toUrl())
        }
    }

    /**
     * getLdapUsers
     *
     * Return the list of ldap users that belong together under the same role.
     *
     * @param na
     */
    def listLdapUsers(def na) {
        def userInstanceList = []
        listLdapGroupUsers(FTP_ROLE + na).each {
            String username = it.split(',')[0].split('=')[1]
            if (springSecurityService.principal.username != username)
                try {
                    userInstanceList << loadUserByUsername(username)
                } catch (NameNotFoundException e) {
                    log.warn(e)
                }
        }
        userInstanceList
    }

    /**
     * updateUser
     *
     * Create or update a LDAP user.
     *
     * @param userInstance
     */
    void updateUser(def params, boolean _createUser) {

        params.password = toggleEnable(params.password, (params.enabled) ?: false)
        params.uidNumber = (params.uidNumber) ?: listLdapUsers(params.na).inject(springSecurityService.principal.uidNumber) { acc, val ->
            if (val.uidNumber > acc)
                acc + 1
            else
                acc
        }

        def person = new LdapUser.Essence();
        person.cn = [params.id]
        person.dn = _usernameMapper.buildDn(params.id)
        person.enabled = (params.enabled) ?: false
        person.gidNumber = params.na as Long
        person.homeDirectory = grailsApplication.config.sa.path + "/" + params.na + "/" + params.uidNumber
        person.loginshell = loginshell
        person.mail = params.mail
        person.password = params.password
        person.sn = params.id
        person.uidNumber = params.uidNumber
        person.username = params.id
        [FTP_ROLE + params.na].each {
            person.addAuthority(new GrantedAuthorityImpl(it))
        }

        if (_createUser)
            createUser(person.createUserDetails())
        else
            updateUser(person.createUserDetails())
    }

    static def manages(def userInstance, def na) {
        userInstance.authorities.find {
            it.authority == 'ROLE_' + FTP_ROLE + na
        }
    }

    /**
     *  toggleEnable
     *
     *  Toggle the ldap password by adding or removing a ! prefix
     *
     * @param password
     * @param enable
     * @return
     */
    private static def toggleEnable(String password, def _enable) {
        boolean enable = _enable as Boolean
        if (enable && password[0] == '!') {
            return password.substring(1)
        } else if (!enable && password[0] != '!') {
            return "!" + password
        }
        password
    }

    /**
     * authentication
     *
     * Create an UsernamePasswordAuthenticationToken for the oauth authentication provider.
     * See config grails.plugins.springsecurity.controllerAnnotations.staticRules:
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
                authorities.collect {
                    new GrantedAuthorityImpl(it)
                }
        )
    }

    def selectKeys(def username) {
        tokenStore.selectKeys(username)
    }

    /***
     * replacekey
     *
     * Replaces the existing key with a new one
     *
     * @param userInstance
     */
    void replacekey(def userInstance) {
        tokenServices.createAccessToken(refresh(userInstance))
    }

    /**
     * refreshkey
     *
     * Refreshed the existing key with new resources
     *
     * @param userInstance
     */
    void refreshkey(def userInstance) {
        tokenStore.updateAuthentication(selectKeys(userInstance.username), refresh(userInstance))
    }

    private OAuth2Authentication refresh(userInstance) {
        removeToken(selectKeys(userInstance.username))
        def client = clientDetailsService.clientDetailsStore.get("clientId")
        ClientToken clientToken = new ClientToken(client.clientId, client.resourceIds as Set<String>,
                client.clientSecret, client.scope as Set<String>, client.authorizedGrantTypes)
        new OAuth2Authentication(clientToken,
                authentication(userInstance, roles(userInstance)))
    }

    void removeToken(def token) {
        if (token) {
            tokenStore.removeAccessTokenUsingRefreshToken(token.refreshToken.value)
            tokenStore.removeRefreshToken(token.refreshToken.value)
        }
    }

    private static def roles(def userInstance) {
        (userInstance.useFor == 'administration') ? ['ROLE_OR_USER', 'ROLE_OR_USER_' + userInstance.na] : ['ROLE_OR_FTPUSER_' + userInstance.username]
    }
}