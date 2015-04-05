import grails.plugin.springsecurity.SpringSecurityUtils
import org.apache.activemq.camel.component.ActiveMQComponent
import org.objectrepository.ai.ldap.UserDetailsContextMapperImpl
import org.objectrepository.ftp.FtpService
import org.objectrepository.instruction.PlanManagerService
import org.objectrepository.ldap.CustomLdapUserDetailsManager
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper
import org.springframework.web.servlet.i18n.SessionLocaleResolver

//noinspection GroovyUnusedAssignment
beans = {

    localeResolver(SessionLocaleResolver) {
        defaultLocale = Locale.ENGLISH
        Locale.setDefault(Locale.ENGLISH)
    }

    if (Boolean.parseBoolean(System.properties.getProperty("plans"))) {
        println("Loading plans")
        planManagerService(PlanManagerService, application) {
            timeout = 10000
        }
    }

    if (Boolean.parseBoolean(System.properties.getProperty("mq")) || Boolean.parseBoolean(System.properties.getProperty("plans"))) {
        println("Loading activemq broker endpoint")
        activemq(ActiveMQComponent) {
            brokerURL = application.config.brokerURL
        }
    }

    if (Boolean.parseBoolean(System.properties.getProperty("ftp"))) {
        println("Loading ftp service")
        ftpService(FtpService) {
            gridFSService = ref('gridFSService')
            grailsApplication = application
            authenticationManager = ref('authenticationManager')
        }
    }

    passwordEncoder(LdapShaPasswordEncoder)

    def conf = SpringSecurityUtils.securityConfig
    if (conf.ldap.active) {
        println("Loading LDAP")
        ldapUserDetailsMapper(UserDetailsContextMapperImpl)
        // Load the LDAP user manager. We rely on the ldap plugin to deliver the contextSource, and dependencies
        usernameMapper(DefaultLdapUsernameToDnMapper,
                conf.ldap.search.base,
                conf.ldap.rememberMe.usernameMapper.usernameAttribute)
        ldapUserDetailsManager(CustomLdapUserDetailsManager, ref('contextSource')) {
            userDetailsMapper = ref('ldapUserDetailsMapper')
            usernameMapper = ref('usernameMapper')
            passwordAttributeName = conf.ldap.mapper.passwordAttributeName
            groupSearchBase = conf.ldap.authorities.groupSearchBase
            groupRoleAttributeName = conf.ldap.authorities.groupRoleAttribute
            groupMemberAttributeName = conf.ldap.rememberMe.detailsManager.groupMemberAttributeName
            grailsApplication = application
            springSecurityService = ref('springSecurityService')

        }
    }

}