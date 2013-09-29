package org.objectrepository.security

import groovy.json.JsonBuilder
import groovy.xml.MarkupBuilder
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
        marshal(userPermission,
                (request.format == 'xml') ? params.user : params,
                (request.format == 'xml') ? 'resources' : 'user'
        )

        if (!userPermission.username)
            return msg(userPermission, 'Expecting: username', 400)


        final authorities = SpringSecurityUtils.authoritiesToRoles(springSecurityService.authentication.authorities).findAll {
            it.startsWith('ROLE_OR_USER_')
        }

        // Does the username exists ?
        def userInstance = User.findByUsername(userPermission.username)
        def password = (userPermission.password) ?: RandomStringUtils.random(6, true, false)
        def encryptedPassword = springSecurityService.encodePassword(password, UUID.randomUUID().encodeAsMD5Bytes())
        if (userInstance) {
            if (!('ROLE_OR_USER_' + userInstance.na in authorities)) {
                return msg(userPermission, 'User ' + userPermission.username + ' already exists and is not under your control. You can only manage users from the authorities ' +
                        authorities.collect { it[13..-1] }.join(', '), 400)
            }
            userProperties.each { p ->
                if (p != 'resources' && userPermission[p]) userInstance[p] = userPermission[p]
            }
            userPermission.refreshKey = true
            if (userPermission.password)
                userInstance.password = encryptedPassword
            else
                password = null
        } else {
            if (!userPermission.mail)
                return msg(userPermission, 'Expecting: mail', 400)
            userInstance = new User(password: encryptedPassword)
            userProperties.each { p ->
                if (userPermission[p]) userInstance[p] = userPermission[p]
            }
        }

        for (def userResourceInstance : userPermission.resources) {
            final countPidOrObjId = gridFSService.countPidOrObjId(params.na, userResourceInstance.pid)
            if (countPidOrObjId.count == 0)
                return msg(userPermission, 'The userResourceInstance with pid ' + userResourceInstance.pid + ' does not exist.', 400)
            if (userResourceInstance.expirationDate && userResourceInstance.expirationDate < new Date())
                userResourceInstance.expirationDate = null
            userResourceInstance.thumbnail = (countPidOrObjId.orfile.level3) ? true : false
            userResourceInstance.contentType = countPidOrObjId.orfile.master.contentType
            userResourceInstance.objid = (countPidOrObjId.orfile.master.metadata.objid) ? true : false
            userInstance.resources.removeAll {
                it.pid == userResourceInstance.pid
            }

            userInstance.resources << userResourceInstance

           /* def orfile = countPidOrObjId.orfile
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
            }*/
        }

        if (!userInstance.save(flush: true)) {
            String error = (userInstance.errors) ? userInstance.errors.allErrors[0].defaultMessage : 'Failed to save user'
            return msg(userPermission, error, 500)
        }

        UserController.roles(userInstance)
        def token = (userPermission.refreshKey) ? ldapUserDetailsManager.refreshKey(userInstance) : ldapUserDetailsManager.replaceKey(userPermission.username)
        userInstance.password = password
        userInstance.url = grailsApplication.config.grails.serverURL + '/' + userPermission.username + '/resource/list?access_token=' + token.value
        msg(userInstance, "ok", 200)
    }

    /**
     * marshal
     *
     * Deep marshalling does not seem to work with our classes. Even when set in the Config file.
     * Hence this effort. ToDo: see in next Grails release if deep marshalling works, so we can remove this method.
     *
     * @param userPermission
     * @param tag
     */
    private static void marshal(def userPermission, def base, String tag) {
        base.findAll {
            it.key.contains('].')
        }.collect {
            [(it.key), base.remove(it.key)]
        }.each { k, v ->
            int index = k[tag.length() + 1] as Integer
            String key = k[tag.length() + 4..-1]
            if (userPermission.resources.size() == index) userPermission.resources << new UserResource()
            switch (key) {
                case 'pid':
                    println(userPermission.resources[index])
                    userPermission.resources[index].pid = v
                    break
                case 'expirationDate':
                    final format = (v.length() == 10) ? t[0..9] : t
                    userPermission.resources[index].expirationDate = Date.parse(format, v)
                    break
                case 'downloadLimit':
                    userPermission.resources[index].downloadLimit = v as Integer
                    break
            }
        }
        def p = (tag == 'user') ? base.user : base
        p.each {
            userPermission.properties[it.key] = it.value
        }
    }

    private void msg(User userInstance, String _message, int statusCode) {

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
            if (statusCode == 200) {
                url userInstance.url
                username userInstance.username
                if (userInstance.password) password userInstance.password
            }
        }

        if (request.format == 'json') builder.writeTo(response.writer)
        response.writer.flush()
        response.writer.close()
    }


}