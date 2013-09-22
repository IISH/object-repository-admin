package org.objectrepository.security

import grails.converters.JSON
import grails.converters.XML
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.access.annotation.Secured

/**
 * UserPermissionController
 *
 * Sets permissions for access to OR resources.
 */
@Secured(['ROLE_OR_USER'])
class UserPermissionController extends NamingAuthorityInterceptor {

    final static String OR = "or_"
    final static String t = "yyyy-MM-dd'T'hh:mm:ss'Z'"
    static {
        grails.converters.JSON.registerObjectMarshaller(UserPermissionMessage) {
            return it.properties.findAll { k, v -> k != 'class' }
        }
        grails.converters.XML.registerObjectMarshaller(UserPermissionMessage) {
            return it.properties.findAll { k, v -> k != 'class' }
        }
    }


    def springSecurityService
    def gridFSService
    def mongo
    def ldapUserDetailsManager
    def tokenStore
    def tokenServices
    def clientDetailsService

    /**
     * save
     *
     * Add or replace access to PID values.
     */
    def save() {

        String username = params.username?.trim()
        if (!username)
            return message('Expecting: username', 400)

        final authorities = SpringSecurityUtils.authoritiesToRoles(springSecurityService.authentication.authorities).findAll {
            it.startsWith('ROLE_OR_USER_')
        }

        // Does the username exists ?
        def userInstance = User.findByUsername(username)
        def password = (params.password) ?: RandomStringUtils.random(6, true, false)
        def token
        def encryptedPassword = springSecurityService.encodePassword(password, UUID.randomUUID().encodeAsMD5Bytes())
        if (userInstance) {
            if (!('ROLE_OR_USER_' + userInstance.na in authorities)) {
                return message('User ' + username + ' already exists and is not under your control. You can only manage users from the authorities ' +
                        authorities.collect { it[13..-1] }.join(', '))
            }
            if (params.useFor) userInstance.useFor = params.useFor
            if (params.mail) userInstance.mail = params.mail
            if (params.password) userInstance.password = encryptedPassword
            token = (params.refreshkey) ? ldapUserDetailsManager.replacekey(userInstance) : ldapUserDetailsManager.selectKeys(username)
        } else {
            userInstance = new User(
                    na: params.na,
                    username: username,
                    password: encryptedPassword,
                    useFor: 'dissemination',
                    enabled: true
            )
            if (params.mail)
                userInstance.mail = params.mail
            else
                return message('Expecting: mail', 400)
            token = ldapUserDetailsManager.replacekey(userInstance)
        }

        def pids = (params.pid instanceof String) ? [params.pid] : params.pid
        if (pids) {
            params.downloadLimit = (params.downloadLimit) ? params.downloadLimit as Integer : null
            final format = (params.expirationDate.length() == 10) ? t[0..9] : t
            params.expirationDate = (params.expirationDate) ? Date.parse(format, params.expirationDate).format(format) : null

            for (String pid : pids) {
                def resource = new UserResource(pid: pid, downloadLimit: params.downloadLimit, expirationDate: params.expirationDate)
                resource.interval = gridFSService.countPidOrObjId(params.na, pid)
                if (resource.interval == 0)
                    return message('The resource with pid ' + pid + ' does not exist.', 400)
                userInstance.resources.removeAll {
                    it.pid = pid
                }
                userInstance.resources << resource
            }
        }

        if (!userInstance.save(flush: true)) {
            return message('Failed to save user', 500)
        }

        String url = grailsApplication.config.grails.serverURL + '/' + username + '/resource/list?access_token=' + token.value
        message("ok", 200, url)

        /*def list = []
        pid.each {
            final orfile = gridFSService.findByPidAsOrfile(it)
            if (orfile)
                grailsApplication.config.buckets.each {
                    if (orfile[it] && orfile[it].metadata?.l) {
                        def d = orfile[it]
                        final String l = "/" + username + d.metadata.l
                        def parent = l
                        int i
                        while ((i = parent.lastIndexOf("/")) > 0) {
                            def child = parent
                            parent = parent.substring(0, i)
                            def n = child.substring(i + 1)
                            if (child.equals(l)) {           // file
                                final f = [n: d.filename, p: d.metadata.pid, l: d.length, t: d.uploadDate.getTime(), a: 'o']
                                if (downloadLimit) f << [l: downloadLimit]
                                if (expirationDate) f << [e: expirationDate]
                                mongo.getDB(OR + params.na).vfs.update([_id: child], [$pull: ['f.p': d.metadata.pid]])
                                mongo.getDB(OR + params.na).vfs.update([_id: child],
                                        [$addToSet: [f: f]],
                                        true, false)
                                list << f
                            }
                            // folder
                            mongo.getDB(OR + params.na).vfs.update([_id: parent], [$addToSet: [d: [n: n]]], true, false)
                        }
                    }
                }
        }

        response.setCharacterEncoding("utf-8")
        response.setContentType("text/xml")
        def builder = new StreamingMarkupBuilder()
        builder.setEncoding("utf-8")
        builder.setUseDoubleQuotes(true)
        response.writer << builder.bind {
            mkp.xmlDeclaration()
            xml() {
                token accessToken.value
                username username
                list.each {
                    id it
                }
            }
        }*/
    }

    private void message(String message, int statusCode, String url = null) {

        final userPermissionMessage = new UserPermissionMessage(url: url, message: message,
                request: params.findAll {
                    it.key != 'action'
                }
        )
        response.status = statusCode
        switch (request.format) {  // withFormat does not seem to work...
            case 'js':
            case 'json':
                render userPermissionMessage as JSON
                break
            default:
                response.status = statusCode
                render userPermissionMessage as XML
                break
        }
    }
}