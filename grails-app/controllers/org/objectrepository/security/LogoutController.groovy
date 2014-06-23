package org.objectrepository.security

import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.access.annotation.Secured

@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class LogoutController {

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index() {
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
	}
}
