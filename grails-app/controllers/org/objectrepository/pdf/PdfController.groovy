package org.objectrepository.pdf

class PdfController {

    def pdfService

    def index() {

        def bucket = params.bucket ?: "level2"
        def list = pdfService.find(params.na, bucket, params.objid)
        if (list) {
            response.setCharacterEncoding("utf-8")
            response.setContentType("application/pdf")
            response.setHeader 'Content-disposition', 'attachment; filename="' + params.objid + '.pdf"'
            pdfService.pdf(response.outputStream, list, bucket, params.cache)
        } else {
            params.pid = params.na + '/' + params.objid
            render(view: '/file/404', status: 404)
        }
    }
}