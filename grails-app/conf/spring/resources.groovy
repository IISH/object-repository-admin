import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.objectrepository.ai.ldap.UserDetailsContextMapperImpl

import org.objectrepository.security.AdminUserDetailsService
import org.socialhistoryservices.security.MongoTokenStore
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.objectrepository.instruction.PlanManager

beans = {

    localeResolver(SessionLocaleResolver) {
        defaultLocale = Locale.ENGLISH
        Locale.setDefault(Locale.ENGLISH)
    }

    if (grailsApplication.config.plans) {
        println("Loading plans")
        planManager(PlanManager, application) {
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
        println("Loading Mongo OAuth2 tokenstore")
        tokenStore(MongoTokenStore) {
            mongo = ref('mongoBean')
            database = "security"
        }
        /*oauth2ProtectedResourceFilter(OAuth2ProtectedResourceFilter) {
            tokenServices = ref('tokenServices')
        }*/
    }
}