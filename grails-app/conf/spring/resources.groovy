import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.objectrepository.ai.ldap.UserDetailsContextMapperImpl
import org.objectrepository.instruction.WorkflowManager
import org.objectrepository.security.AdminUserDetailsService
import org.socialhistoryservices.security.MongoOAuth2ProviderTokenServices
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager
import org.springframework.security.oauth2.provider.OAuth2ProtectedResourceFilter
import org.springframework.web.servlet.i18n.SessionLocaleResolver

beans = {

    localeResolver(SessionLocaleResolver) {
        defaultLocale = Locale.ENGLISH
        Locale.setDefault(Locale.ENGLISH)
    }

    if (grailsApplication.config.wf) {
        workflowManager(WorkflowManager, application) {
            timeout = 10000
        }
    }

    userDetailsService(AdminUserDetailsService)

    def conf = SpringSecurityUtils.securityConfig
    if (conf.ldap.active) {
        println("Loading LDAP")
        ldapUserDetailsMapper(UserDetailsContextMapperImpl)
        // Load the LDAP user manager. We rely on the ldap plugin to deliver the contextSource, and dependencies
        usernameMapper(DefaultLdapUsernameToDnMapper,
                conf.ldap.search.base,
                conf.ldap.rememberMe.usernameMapper.usernameAttribute)
        ldapUserDetailsManager(LdapUserDetailsManager, ref('contextSource')) {
            userDetailsMapper = ref('ldapUserDetailsMapper')
            usernameMapper = ref('usernameMapper')
            passwordAttributeName = conf.ldap.mapper.passwordAttributeName
            groupSearchBase = conf.ldap.authorities.groupSearchBase
            groupRoleAttributeName = conf.ldap.authorities.groupRoleAttribute
            groupMemberAttributeName = conf.ldap.rememberMe.detailsManager.groupMemberAttributeName
        }
    }

    if (conf.oauthProvider.active) {
        println("Loading OAuth2")
        oauthTokenServices(MongoOAuth2ProviderTokenServices, ref('mongoBean')) {
            database = "security"
            clientId = "clientId"
            //authorizedGrantTypes = client.authorizedGrantTypes
            reuseRefreshToken = conf.oauthProvider.tokenServices.reuseRefreshToken    // true
            supportRefreshToken = conf.oauthProvider.tokenServices.supportRefreshToken    // true
            refreshTokenValiditySeconds = 31556925
            accessTokenValiditySeconds = 31556925
        }
        oauth2ProtectedResourceFilter(OAuth2ProtectedResourceFilter) {
            tokenServices = ref('oauthTokenServices')
        }
    }
}