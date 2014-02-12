package org.objectrepository.mets

/**
 * Offer a mets document on the fly based on a PID value or label
 */
class MetsController {

    def metsService

    def index() {

        if (request.method.toLowerCase() == 'head') {
            if (metsService.countMetsFile(params.na, params.objid) == 0)
                return render(status: 404)
            else
                return render(status: 200, characterEncoding: 'utf-8', contentType: 'text/xml')
        }

        def xml = metsService.writeMetsFile(params.na, params.objid)
        if (xml) {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/xml")
            response.writer.write(xml)
            response.writer.flush()
        } else {
            params.pid = params.na + '/' + params.objid
            render(view: '/file/404', status: 404)
        }
    }
}
