package org.objectrepository.mets

import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured

/**
 * Offer a mets document on the fly based on a PID value or label
 */
@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class MetsController {

    def metsService

    def index(String na, String objid) {

        if (request.method.toLowerCase() == 'head') {
            if (metsService.countMetsFile(na, objid) == 0)
                return render(status: HttpStatus.NOT_FOUND.value())
            else
                return render(status: HttpStatus.OK.value(), characterEncoding: 'utf-8', contentType: 'text/xml')
        }

        def xml = metsService.writeMetsFile(na, objid)
        if (xml) {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/xml")
            response.writer.write(xml)
            response.writer.flush()
        } else {
            params.pid = na + '/' + objid
            render(view: '/file/404', status: HttpStatus.NOT_FOUND.value())
        }
    }
}
