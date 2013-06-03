package org.objectrepository.mets

import grails.converters.XML
import groovy.xml.MarkupBuilder

/**
 * Offer a mets document on the fly based on a PID value or label
 */
class MetsController {

    def metsService

    def index() {

        def xml = metsService.writeMetsFile(params.na, params.objid)
        if (xml) {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/xml")
            response.writer.write(xml)
        } else {
            params.pid = params.na + '/' + params.objid
            render(view: '/file/404', status: 404)
        }
    }
}
