package org.objectrepository.mets

/**
 * Offer a mets document on the fly based on a PID value or label
 */
class MetsController {

    def metsService

    def index() {

        def mets = metsService.writeMetsFile(params.na, params.label, Boolean.parseBoolean(params.cache))
        if (mets) {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/xml")
            mets.write(response.outputStream)
            response.outputStream.flush()
        }

    }
}
