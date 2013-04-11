package org.objectrepository.instruction

import com.mongodb.DBCursor
import groovy.xml.StreamingMarkupBuilder
import org.objectrepository.util.OrUtil
import org.springframework.context.i18n.LocaleContextHolder

class DownloadService {

    static transactional = 'mongo'

    def _filter = ['version', '_id', '_class', 'date', 'name', 'na', 'fileSet',
            'pidwebserviceEndpoint', 'pidwebserviceKey', 'task', 'workflow', 'length', 'plan']

    def messageSource
    def gridFSService
    def grailsApplication

    /**
     * write
     *
     * Output the XML to the writer.
     * The file types are fetched with a cursor,
     * so massive lists of files should also be able to be handled this
     * way
     */
    void write(def writer, def document) {

        def instructionAttributes = [xmlns: "http://objectrepository.org/instruction/1.0/"]
        instructionAttributes.putAll(OrUtil.getPropertiesMap(document, true, _filter))
        OrUtil.setInstructionPlan(document)
        instructionAttributes << [plan: document.plan.collect() {
                    it
                }.join(",")]

        def builder = new StreamingMarkupBuilder()
        builder.setEncoding("utf-8")
        builder.setUseDoubleQuotes(true)

        final DBCursor cursor = document.findFilesWithCursor()
        writer << builder.bind {
            mkp.xmlDeclaration()
            comment << 'Instruction extracted on ' + new Date().toGMTString() + " from fileSet " + document.fileSetAlias
            instruction(instructionAttributes) {
                while (cursor.hasNext()) {
                    out << writeStagingfile(cursor.next().toMap())
                }
            }
        }

        writer.flush()
        writer.close()
    }

    /**
     * Helper of download controller. Writes a single Stagingfile as XML.
     * @param map The map representing a single Stagingfile's attributes.
     * @return The XML representation in form of MarkupBuilder.
     */
    def writeStagingfile(Map map) {
        def task = map.remove("task");
        return {
            stagingfile {
                map.each { key, value ->
                    //noinspection GroovyAssignabilityCheck
                    if (value && !(key in _filter)) "$key" value
                }
                if (task) {
                    def code = task.name + "." + task.statusCode
                    def name = (task.name == 'InstructionValidate') ? 'error' : 'task'
                    //noinspection GroovyAssignabilityCheck
                    "$name" {
                        statusCode code
                        status messageSource.getMessage(code + '.status', null, null, LocaleContextHolder.locale)
                        info messageSource.getMessage(code + '.info', null, task.info, LocaleContextHolder.locale)
                    }
                }
            }
        }
    }

    /**
     * writeOrfiles
     *
     * Partial data dump of all technical metadata.
     *
     * @param id Identifier of the document. If null all metadata is downloaded.
     * @param na
     * @param writer
     */
    void writeOrfiles(def params, def writer) {

        def orfileAttributes = [xmlns: "http://objectrepository.org/orfiles/1.0/"]

        def builder = new StreamingMarkupBuilder()
        builder.setEncoding("utf-8")
        builder.setUseDoubleQuotes(true)

        def list = (params.label) ? gridFSService.listFilesByLabel(params.na, params.label) : [gridFSService.findByPid(params.pid)]
        writer << builder.bind {
            mkp.xmlDeclaration()
            comment << String.format('Selection contains %s documents. Export extracted on %s',
                    list.size(), new Date().toGMTString())
            orfiles(orfileAttributes) {
                list.each { orfileInstance ->
                    orfile {
                        pid orfileInstance.metadata.pid
                        resolverBaseUrl orfileInstance.metadata.resolverBaseUrl
                        pidurl orfileInstance.metadata.resolverBaseUrl + orfileInstance.metadata.pid
                        if (orfileInstance.metadata.lid) { lid orfileInstance.metadata.lid }
                        if (orfileInstance.metadata.objid) {
                            objid orfileInstance.metadata.resolverBaseUrl + orfileInstance.metadata.objid
                            seq orfileInstance.metadata.seq
                        }
                        filename orfileInstance.filename
                        label orfileInstance.metadata.label
                        access orfileInstance.metadata.acc
                        out << metadata(orfileInstance, "master")
                        ['level1', 'level2', 'level3'].each {  bucket ->
                            out << metadata(gridFSService.findByPid(orfileInstance.metadata.pid, bucket), bucket)
                        }
                    }
                }
            }
            writer.flush()
        }
    }

    private metadata(def orfileInstance, def bucket) {
        if (orfileInstance)
            return {
                final String _resolveUrl = grailsApplication.config.grails.serverURL + "/file/" + bucket + "/" + orfileInstance.metadata.pid
                "$bucket" {
                    if (orfileInstance.metadata.pidType == "or") {
                        pidurl orfileInstance.metadata.resolverBaseUrl + orfileInstance.metadata.pid + "?locatt=view:" + bucket
                    } else {
                        pidurl orfileInstance.metadata.resolverBaseUrl + orfileInstance.metadata.pid
                    }
                    resolveUrl _resolveUrl
                    ['contentType',
                            'length',
                            'content',
                            'md5',
                            'uploadDate',
                            'firstUploadDate',
                            'lastUploadDate',
                            'timesAccessed',
                            'timesUpdated'].each {
                        if (orfileInstance[it]) {
                            "$it" orfileInstance[it]
                        } else if (orfileInstance.metadata[it]) {
                            "$it" orfileInstance.metadata[it]
                        }
                    }
                }

            }
    }
}
