package org.objectrepository.security

import groovy.xml.StreamingMarkupBuilder
import org.springframework.security.access.annotation.Secured
import org.springframework.security.oauth2.provider.ClientToken
import org.springframework.security.oauth2.provider.OAuth2Authentication

/**
 * UserPermissionController
 *
 * Sets permissions for access to OR resources.
 */
@Secured(['ROLE_OR_USER'])
class UserPermissionController extends NamingAuthorityInterceptor {

    final static String OR = "or_"
    final static String t = "yyyy-MM-dd'T'hh:mm:ss'Z'"


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

        def permission = new UserResource(params.permission)
        assert permission.pid
        assert permission.username

        String user = params.username.trim()
        assert user != params.na
        def pid = (params.pid instanceof String) ? [params.pid] : params.pid
        String downloadLimit = (params.downloadLimit) ? params.downloadLimit as Integer : null
        final format = (params.expirationDate.length() == 10) ? t[0..9] : t
        String expirationDate = (params.expirationDate) ? Date.parse(format, params.expirationDate).format(format) : null

        def list = []
        pid.each {
            final orfile = gridFSService.findByPidAsOrfile(it)
            if (orfile)
                grailsApplication.config.buckets.each {
                    if (orfile[it] && orfile[it].metadata?.l) {
                        def d = orfile[it]
                        final String l = "/" + user + d.metadata.l
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

        def accessToken = tokenStore.selectKeys(user)
        if (!accessToken) {
            def client = clientDetailsService.clientDetailsStore.get("clientId")
            ClientToken clientToken = new ClientToken(client.clientId, client.resourceIds as Set<String>,
                    client.clientSecret, client.scope as Set<String>, client.authorizedGrantTypes)
            final OAuth2Authentication authentication = new OAuth2Authentication(clientToken,
                    ldapUserDetailsManager.authentication(user, ['ROLE_OR_USER_' + user]))
            accessToken = tokenServices.createAccessToken(authentication)
        }

        assert accessToken?.value

        response.setCharacterEncoding("utf-8")
        response.setContentType("text/xml")
        def builder = new StreamingMarkupBuilder()
        builder.setEncoding("utf-8")
        builder.setUseDoubleQuotes(true)
        response.writer << builder.bind {
            mkp.xmlDeclaration()
            xml() {
                token accessToken.value
                username user
                list.each {
                    id it
                }
            }
        }
    }

}
