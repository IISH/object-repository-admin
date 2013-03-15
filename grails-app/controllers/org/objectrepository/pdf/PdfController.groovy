package org.objectrepository.pdf

class PdfController {

    def pdfService

    def index() {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/pdf")
        pdfService.list(response.outputStream, params.na, params.objid, params.bucket ?: "level2")
    }
}
