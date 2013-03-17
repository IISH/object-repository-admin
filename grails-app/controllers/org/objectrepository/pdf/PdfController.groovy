package org.objectrepository.pdf

class PdfController {

    def pdfService

    def index() {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/pdf")
        response.setHeader 'Content-disposition', 'attachment; filename="' + params.objid + '.pdf"'
        pdfService.list(response.outputStream, params.na, params.objid, params.bucket ?: "level2")
    }
}
