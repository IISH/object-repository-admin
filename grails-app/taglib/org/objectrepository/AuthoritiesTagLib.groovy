package org.objectrepository

import org.objectrepository.security.UserRole
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class AuthoritiesTagLib {
    static namespace = 'roles'

    def springSecurityService

    def list = { attrs ->

        def roles = SpringSecurityUtils.authoritiesToRoles(springSecurityService.principal.authorities).findAll {
            it.startsWith('ROLE_OR_USER_')
        }
        if (roles.size() > 1) {
            out << '<ul>'
            roles.each {
                def na = it.split('_').last()
                out << '<li>'
                out << g.link([mapping: 'namingAuthority', params: [na: na]] , na)
                out << '</li>'
            }
            out << '</ul>'
        }
    }
}
