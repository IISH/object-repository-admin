package org.objectrepository.administration

import org.objectrepository.security.NamingAuthorityInterceptor
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class HealthController extends NamingAuthorityInterceptor {

    def springSecurityService


    def index = {
        def f = new File(grailsApplication.config.health_folder + '/' + params.na + '.html')
        if ( f.exists() ) {
            response.setCharacterEncoding('utf-8')
            response.setContentType('text/html')
            def writer = response.outputStream
            f.eachLine {
                writer.write(it.bytes)
            }
            writer.close()
        } else {
            render(view: '/file/404', status: 404)
        }
    }
}