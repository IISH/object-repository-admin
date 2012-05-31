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

class BootStrap {

    def springSecurityService
    def grailsApplication
    def clientDetailsService
    def planManager

    def init = { servletContext ->

        bindSpringSecurityService()
        users()
        oauth2()
        bindAccessors()
        if (planManager) {
            planManager.start();
        }
    }

    /**
     * users
     *
     * Adds default SuperAdmin user and test users.
     */
    private void users() {

        if (grailsApplication.config.addUsers) {
            switch (Environment.current) {
                case Environment.PRODUCTION:
                    addUser("0", "admin", "ROLE_ADMIN")
                    break
                case Environment.DEVELOPMENT:
                    addUser("0", "admin", "ROLE_ADMIN")
                    addUser("12345", "12345", "ROLE_CPADMIN")
                    addUser("20000", "20000", "ROLE_CPADMIN")
                    break
                case Environment.TEST:
                    addUser("12345", "12345", "ROLE_CPADMIN")
                    break
            }
        }
    }

    private void bindSpringSecurityService() {

        // Add helpers methods:
        springSecurityService.metaClass.hasRole = { def role = 'ROLE_ADMIN' ->
            role in roles
        }
        springSecurityService.metaClass.getRoles() {
            principal.authorities*.authority
        }
        springSecurityService.metaClass.hasValidNa = { def na ->
            hasRole('ROLE_ADMIN') || na == getPrincipal().na  // Any user with an NA is authorized
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

    private void addUser(String na, String username, String authority) {

        log.info "Add user for na " + na + " username " + username + " authority " + authority
        def role = (Role.findByAuthority(authority)) ?: new Role(authority: authority).save(failOnError: true)
        def password = springSecurityService.encodePassword(username)
        def user = User.findByUsername(username) ?: new User(username: username, password: password,
                na: na, o: 'socialhistoryservices', mail: username + '@socialhistoryservices.org').save(failOnError: true)
        if (!(role in user.authorities)) {
            UserRole.create user, role
        }

        OrUtil.availablePolicies(na, grailsApplication.config.accessMatrix)
        if (!Profile.findByNa(na)) new Profile(na: na).save(failOnError: true)
    }

    def destroy = {
        if (planManager) planManager.cleanup()
    }
}