import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.objectrepository.ai.ldap.UserDetailsContextMapperImpl
import org.objectrepository.instruction.PlanManagerService
import org.socialhistoryservices.security.MongoTokenStore
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.objectrepository.ftp.FtpService
import org.objectrepository.ldap.CustomLdapUserDetailsManager
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder

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

    if (conf.oauthProvider.active) {
        println("Loading Mongo OAuth2 tokenstore")
        tokenStore(MongoTokenStore) {
            mongo = ref('mongoBean')
            database = "security"
        }
    }
}