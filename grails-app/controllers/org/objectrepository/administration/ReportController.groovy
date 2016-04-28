package org.objectrepository.administration

import org.objectrepository.security.NamingAuthorityInterceptor
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class ReportController extends NamingAuthorityInterceptor {

    def springSecurityService


    def index = {
        [files: new File(grailsApplication.config.report_folder + '/' + params.na).listFiles().collect{ it.name }.sort().reverse()]
    }

    def show(String id) {

        def f = new File(grailsApplication.config.report_folder + '/' + params.na +'/' + id)
        if ( f.exists() ) {
            response.setCharacterEncoding('utf-8')
            response.setContentType('text/plain')
            def writer = response.outputStream
            f.eachLine {
                writer.write(it.bytes)
                writer.write(13)
            }
            writer.close()
        } else {
            params.pid = id
            render(view: '/file/404', status: 404)
        }
    }

}