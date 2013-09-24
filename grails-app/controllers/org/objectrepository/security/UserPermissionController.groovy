package org.objectrepository.security

import grails.converters.JSON
import grails.converters.XML
import groovy.json.JsonBuilder
import groovy.xml.MarkupBuilder
import groovy.xml.StreamingMarkupBuilder
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.access.annotation.Secured

/**
 * UserController
 *
 * Sets permissions for access to OR resources.
 */
@Secured(['ROLE_OR_USER'])
class UserPermissionController extends NamingAuthorityInterceptor {

    final static String OR = "or_"
    final static String t = "yyyy-MM-dd'T'hh:mm:ss'Z'"

    static userProperties

    UserPermissionController() {
        userProperties = new DefaultGrailsDomainClass(User.class).persistentProperties.collect { it.name }
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

        // Deep conversion does not seem to tricker, so...
        def userPermission = new User(na: params.na, resources: [])
        params.user.findAll {
            it.key.startsWith('resources')
        }.collect {
            [(it.key), params.user.remove(it.key)]
        }.each { k, v ->
            int index = k[10] as Integer
            String key = k[13..-1]
            if (userPermission.resources.size() == index) userPermission.resources << new UserResource()
            switch (key) {
                case 'pid':
                    userPermission.resources[index].pid = v
                    break
                case 'expirationDate':
                    final format = (v.length() == 10) ? t[0..9] : t
                    userPermission.resources[index].expirationDate = Date.parse(format, v)
                    break;
                case 'downloadLimit':
                    userPermission.resources[index].downloadLimit = v as Integer
                    break;
            }
        }
        params.user.each {
            userPermission.properties[it.key] = it.value
        }

        if (!userPermission.username)
            return message(userPermission, 'Expecting: username', 400)


        final authorities = SpringSecurityUtils.authoritiesToRoles(springSecurityService.authentication.authorities).findAll {
            it.startsWith('ROLE_OR_USER_')
        }

        // Does the username exists ?
        def userInstance = User.findByUsername(userPermission.username)
        def password = (userPermission.password) ?: RandomStringUtils.random(6, true, false)
        def encryptedPassword = springSecurityService.encodePassword(password, UUID.randomUUID().encodeAsMD5Bytes())
        def token
        if (userInstance) {
            if (!('ROLE_OR_USER_' + userInstance.na in authorities)) {
                return message(userPermission, 'User ' + userPermission.username + ' already exists and is not under your control. You can only manage users from the authorities ' +
                        authorities.collect { it[13..-1] }.join(', '))
            }
            userProperties.each { p ->
                if (p != 'resources' && userPermission[p]) userInstance[p] = userPermission[p]
            }
            if (userPermission.password) userInstance.password = encryptedPassword
            token = (userPermission.refreshKey) ? ldapUserDetailsManager.replacekey(userInstance) : ldapUserDetailsManager.selectKeys(userPermission.username)
        } else {
            if (!userPermission.mail)
                return message(userPermission, 'Expecting: mail', 400)
            userInstance = new User(password: encryptedPassword)
            userProperties.each { p ->
                if (userPermission[p]) userInstance[p] = userPermission[p]
            }
            token = ldapUserDetailsManager.replacekey(userInstance)
        }

        for (def userResourceInstance : userPermission.resources) {
            final countPidOrObjId = gridFSService.countPidOrObjId(params.na, userResourceInstance.pid)
            userResourceInstance.interval = countPidOrObjId.count
            if (userResourceInstance.interval == 0)
                return message(userPermission, 'The userResourceInstance with pid ' + userResourceInstance.pid + ' does not exist.', 400)
            if (userResourceInstance.expirationDate && userResourceInstance.expirationDate < new Date())
                userResourceInstance.expirationDate = null
            userResourceInstance.thumbnail = (countPidOrObjId.orfile.level3) ? true : false
            userResourceInstance.contentType = countPidOrObjId.orfile.master.contentType
            userResourceInstance.objid = (countPidOrObjId.orfile.master.metadata.objid) ? true : false
            userInstance.resources.removeAll {
                it.pid == userResourceInstance.pid
            }

            userInstance.resources << userResourceInstance
        }

        if (!userInstance.save(flush: true)) {
            String error = (userInstance.errors) ? userInstance.errors.allErrors[0].defaultMessage : 'Failed to save user'
            return message(userPermission, error, 500)
        }

        userInstance.password = password
        userInstance.url = grailsApplication.config.grails.serverURL + '/' + userPermission.username + '/resource/list?access_token=' + token.value
        message(userInstance, "ok", 200)

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
        */
    }

    private void message(User userInstance, String _message, int statusCode) {

        response.status = statusCode
        response.setCharacterEncoding("utf-8")
        def builder
        switch (request.format) {  // withFormat does not seem to work...
            case 'js':
            case 'json':
                response.setContentType("text/javascript")
                builder = new JsonBuilder()
                break
            case 'xml':
            default:
                response.setContentType("text/xml")
                builder = new MarkupBuilder(response.writer)
                break
        }

        builder.user {
            message _message
            url userInstance.url
            username userInstance.username
            password userInstance.password
        }

        if ( builder instanceof JsonBuilder ) response.writer.write( builder.toString() )
        response.writer.close()
    }


}