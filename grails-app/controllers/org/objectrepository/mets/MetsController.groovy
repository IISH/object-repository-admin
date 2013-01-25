package org.objectrepository.mets

/**
 * Offer a mets document on the fly based on a PID value or label
 */
class MetsController {

    def downloadMetsService

    def index() {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/xml")
        downloadMetsService.writeMetsFile(params, response.outputStream)
        response.writer.flush()
        return null
    }
}
