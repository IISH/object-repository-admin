package org.objectrepository.security

import org.apache.commons.lang.RandomStringUtils
import org.springframework.security.access.annotation.Secured

/**
 * UserController
 *
 * We will use LDAP for creating ftp accounts. Not user accounts.
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 */
@Secured(['ROLE_OR_USER'])
class StagingareaController extends NamingAuthorityInterceptor {

    def springSecurityService
    def grailsApplication

    def index = {
        [ldaphowto: grailsApplication.config.ldaphowto]
    }

}