package org.objectrepository.security

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class LogoutController {

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
	}
}
