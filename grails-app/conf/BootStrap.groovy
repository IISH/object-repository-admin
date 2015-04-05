import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Environment
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.objectrepository.instruction.Instruction
import org.objectrepository.instruction.Profile
import org.objectrepository.instruction.Stagingfile
import org.objectrepository.security.Role
import org.objectrepository.security.User
import org.objectrepository.security.UserRole
import org.objectrepository.util.OrUtil
import org.springframework.security.oauth2.provider.client.BaseClientDetails


class BootStrap {

    def springSecurityService
    def ldapUserDetailsManager
    def grailsApplication
    def clientDetailsService
    def planManagerService
    def ftpService
    def activemq

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
                    addUser(['0'], adminUsername,
                            springSecurityService.encodePassword(adminPassword, UUID.randomUUID().encodeAsMD5Bytes()),
                    ['administrator'])
                }
                break
            case Environment.DEVELOPMENT:
                addUser(["0"], "admin")
                addUser(["12345", "10622", "11152"], "12345")
                addUser(["20000"], "20000")
                break
            case Environment.TEST:
                addUser(new Role(authority: 'ROLE_OR_USER').save(failOnError: true), "12345", "12345")
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
            def authorities = authentication.authorities*.role.findAll {
                it.startsWith('ROLE_OR_USER_')
            }.sort { it }
            if (authorities) ((String)authorities[0]).split('_').last()
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

    private void addUser(def na, String username, String password = null, def roles = null) {

        User.findByUsername(username)?.delete(flush: true)

        log.info "Add user username " + username
        if (!password) password = springSecurityService.encodePassword(username)
        def user = new User(username: username, password: password,
                mail: username + '@socialhistoryservices.org').save(failOnError: true)

        final defaultRole = Role.findByAuthority('ROLE_OR_USER') ?: new Role(authority: 'ROLE_OR_USER').save(failOnError: true)
        UserRole.create user, defaultRole
        na.each {
            def authority = "ROLE_OR_USER_" + it
            log.info "Add authority " + authority
            def role = Role.findByAuthority(authority) ?: new Role(authority: authority).save(failOnError: true)
            UserRole.create user, role

            OrUtil.availablePolicies(it, grailsApplication.config.accessMatrix)
            if (!Profile.findByNa(it)) new Profile(na: it).save(failOnError: true)
        }

        roles?.each {
            def authority = "ROLE_OR_USER_" + it
            log.info "Add authority " + authority
            def role = Role.findByAuthority(authority) ?: new Role(authority: authority).save(failOnError: true)
            UserRole.create user, role
        }
    }
}