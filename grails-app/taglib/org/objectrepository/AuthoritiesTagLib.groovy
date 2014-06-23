package org.objectrepository

import grails.plugin.springsecurity.SpringSecurityUtils

class AuthoritiesTagLib {
    static namespace = 'roles'

    def springSecurityService

    def list = { attrs ->

        def roles = (springSecurityService.isLoggedIn()) ?
            SpringSecurityUtils.authoritiesToRoles(springSecurityService.authentication.authorities).findAll {
                it.startsWith('ROLE_OR_USER_')
            } : []

        if (roles.size() > 1) {
            out << '<ul>'
            roles.each {
                def na = it.split('_').last()
                if (params.na == na)
                    out << '<li style="background:#cccccc">'
                else
                    out << '<li>'
                out << g.link([mapping: 'namingAuthority', params: [na: na]], na)
                out << '</li>'
            }
            out << '</ul>'
        }
    }
}
