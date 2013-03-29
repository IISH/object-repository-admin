import grails.util.Environment
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.objectrepository.instruction.Instruction
import org.objectrepository.instruction.Profile
import org.objectrepository.instruction.Stagingfile
import org.objectrepository.security.Role
import org.objectrepository.security.User
import org.objectrepository.security.UserRole
import org.objectrepository.util.OrUtil
import org.springframework.security.oauth2.provider.BaseClientDetails
import org.springframework.ldap.core.DistinguishedName
import org.springframework.security.core.GrantedAuthority
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.ContextMapper
import org.springframework.ldap.NamingException
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager

class BootStrap {

    def springSecurityService
    def LdapUserDetailsManager ldapUserDetailsManager
    def grailsApplication
    def clientDetailsService
    def planManagerService
    def ftpService

    def init = { servletContext ->

        bindSpringSecurityService()
        users()
        oauth2()
        bindAccessors()
        planManagerService?.start();
        ftpService?.start()
    }

    /**
     * users
     *
     * Adds default SuperAdmin user and test users.
     * In production the superadmin is set at startup of the application with the VM parameters:
     * -DadminUsername and -DadminPassword
     *
     * In development mode test accounts are created
     */
    private void users() {

        switch (Environment.current) {
            case Environment.PRODUCTION:
                final String adminUsername = System.getProperty("adminUsername")
                final String adminPassword = System.getProperty("adminPassword")
                if (adminPassword && adminUsername) {
                    addUser("0", adminUsername, springSecurityService.encodePassword(adminPassword))
                }
                break
            case Environment.DEVELOPMENT:
                addUser("0", "admin")
                addUser("12345", "12345")
                addUser("20000", "20000")
                break
            case Environment.TEST:
                addUser("12345", "12345")
                break
        }
    }

    private void bindSpringSecurityService() {

        // Add helpers methods:
        springSecurityService.metaClass.hasNa = { def na ->
            def role = "ROLE_OR_USER_" + na
            na && (role in authentication.authorities*.authority)
        }

        springSecurityService.metaClass.getNa = {
            authentication.authorities*.role.find {
                it.startsWith('ROLE_OR_USER_')
            }?.split('_').last()
        }
    }

    private void oauth2() {

        if (SpringSecurityUtils.securityConfig.oauthProvider.active) {

            def client = new BaseClientDetails()
            client.clientId = 'clientId'
            client.authorizedGrantTypes = ["authorization_code", "refresh_token", "client_credentials"]
            clientDetailsService.clientDetailsStore = [
                    "clientId": client
            ]
        }
    }

    /**
     * bindAccessors
     *
     * Contains the generic elements for the Stagingfile (most that is), Profile and Instruction instances
     * As null values from the Stagingfile are supplied by corresponding attributes from Instruction; and
     * in it's turn an instance of Instruction receives substitute values from the Profile instance as well; we use a
     * dynamic way to write this in the Bootstrap.
     */
    private void bindAccessors() {

        new DefaultGrailsDomainClass(Profile.class).persistentProperties.each { p ->
            String attribute = p.name
            log.info "bindAccessors: " + attribute
            String getter = "get" + OrUtil.camelCase([attribute])
            String setter = "set" + OrUtil.camelCase([attribute])

            [Stagingfile, Instruction].each {
                it.metaClass."$getter" = {
                    // we will not use the elvis operator ?: because it does not distinguish between null and the boolean false.
                    delegate.@"$attribute" = (delegate.@"$attribute" == null) ? parent."$attribute" : delegate.@"$attribute"
                }
                // As we overwrite the getter, we need set the setter to prevent loop-ness
                it.metaClass."$setter" = { val ->
                    delegate.@"$attribute" = val
                }
            }
        }
    }

    private void addUser(String na, String username, String password = null) {

        def authority = "ROLE_OR_USER_" + na
        log.info "Add user for na " + na + " username " + username + " authority " + authority
        def role = (Role.findByAuthority(authority)) ?: new Role(authority: authority).save(failOnError: true)
        if (!password) password = springSecurityService.encodePassword(username)
        def user = User.findByUsername(username) ?: new User(username: username, password: password,
                mail: username + '@socialhistoryservices.org').save(failOnError: true)
        if (!user.authorities.find {
            it.id == role.id
        }
        ) UserRole.create user, role
    }
}