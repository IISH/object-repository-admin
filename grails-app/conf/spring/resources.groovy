import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.objectrepository.ai.ldap.UserDetailsContextMapperImpl
import org.objectrepository.security.AdminUserDetailsService
import org.socialhistoryservices.security.MongoOAuth2ProviderTokenServices
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SimpleTriggerBean
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager
import org.springframework.security.oauth2.provider.OAuth2ProtectedResourceFilter
import org.springframework.web.servlet.i18n.SessionLocaleResolver

beans = {

    localeResolver(SessionLocaleResolver) {
        defaultLocale = Locale.ENGLISH
        Locale.setDefault(Locale.ENGLISH)
    }

    if (application.config.wf) {
        println("Loading workflow")

        // How can we dry this ?
        jobWorkflowInitiateService(MethodInvokingJobDetailFactoryBean) {
            targetObject = ref('workflowInitiateService')
            targetMethod = 'job'
        }
        jobWorkflowActiveService(MethodInvokingJobDetailFactoryBean) {
            targetObject = ref('workflowActiveService')
            targetMethod = 'job'
        }
        jobWorkflowStaleService(MethodInvokingJobDetailFactoryBean) {
            targetObject = ref('workflowStaleService')
            targetMethod = 'job'
        }
        simpleTriggerWorkflowStaleService(SimpleTriggerBean) {
            jobDetail = ref('jobWorkflowStaleService')
            startDelay = 5000
            repeatInterval = 10000
        }
        simpleTriggerInitiateService(SimpleTriggerBean) {
            jobDetail = ref('jobWorkflowInitiateService')
            startDelay = 10000
            repeatInterval = 10000
        }
        simpleTriggerWorkflowActiveService(SimpleTriggerBean) {
            jobDetail = ref('jobWorkflowActiveService')
            startDelay = 15000
            repeatInterval = 10000
        }
        triggers(SchedulerFactoryBean) {
            triggers = [ref('simpleTriggerInitiateService'), ref('simpleTriggerWorkflowActiveService'), ref('simpleTriggerWorkflowStaleService')]
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