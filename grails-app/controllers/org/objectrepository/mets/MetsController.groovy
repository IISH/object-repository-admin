package org.objectrepository.mets

/**
 * Offer a mets document on the fly based on a PID value or label
 */
class MetsController {

    def downloadMetsService

    def index() {

        def mets = downloadMetsService.writeMetsFile(params.na, params.label)
        if (mets) {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/xml")
            mets.write(response.outputStream)
            response.writer.flush()
            return null
        }
    }
}
