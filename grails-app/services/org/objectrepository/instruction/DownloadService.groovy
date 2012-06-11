package org.objectrepository.instruction

import com.mongodb.DBCursor
import org.objectrepository.util.OrUtil
import groovy.xml.StreamingMarkupBuilder
import org.springframework.context.i18n.LocaleContextHolder

class DownloadService {

    static transactional = 'mongo'

    def _filter = ['version', '_id', '_class', 'date', 'name', 'na', 'fileSet',
            'pidwebserviceEndpoint', 'pidwebserviceKey', 'task', 'workflow', 'length', 'plan']

    def messageSource

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
}
