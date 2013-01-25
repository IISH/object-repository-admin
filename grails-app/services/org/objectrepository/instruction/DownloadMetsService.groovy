package org.objectrepository.instruction

import au.edu.apsr.mtk.base.METSWrapper
import com.mongodb.DBCursor
import groovy.xml.StreamingMarkupBuilder

class DownloadMetsService {

    static transactional = 'mongo'
    static String OR = "or_"
    static def uses = [master: 'archive', level1: 'hires reference', level2: 'reference', level3: 'thumbnail']
    def mongo
    def grailsApplication
    def gridFSService

    /**
     * writeMetsFile
     *
     * Output the XML to the writer.
     *
     * Strategy:
     *  - collect the documents by label
     *  - for each found level, produce a fileSec  Then write. And collect the relationship for the structural map in a map.
     *  - write the structural map
     */
    void writeMetsFile(def params, def writer) {

        def builder = new StreamingMarkupBuilder()
        builder.setEncoding("utf-8")
        builder.setUseDoubleQuotes(true)

        final collection = mongo.getDB(OR + params.na).getCollection("master.files")
        DBCursor cursor = collection.find([$or: [['metadata.label': params.label], ['metadata.pid': params.label]]], ['metadata.pid': 1])
        if (cursor.count() == 0) return

        METSWrapper metsWrapper = new METSWrapper()
        final mets = metsWrapper.getMETSObject()
        final fileSection = mets.newFileSec()
        mets.setFileSec(fileSection)
        final structMap = mets.newStructMap()
        mets.addStructMap(structMap)
        structMap.setType("physical");
        final divMain = structMap.newDiv()
        divMain.setType("container")
        structMap.addDiv(divMain)

        int count = 0
        int _file_ID = 0
        int _group_ID = 0
        while (cursor.hasNext()) {
            _group_ID++
            def doc = gridFSService.get(params.na, cursor.next().metadata.pid)
            doc.each { def d ->
                String _use = (uses[d.key]) ?: d.key
                String type = d.value.contentType.split('/')[0]
                String use = _use + " " + type
                def fileGrps = fileSection.getFileGrpByUse(use)
                def fileGrp = (fileGrps.size() == 0) ? null : fileGrps[0]
                if (!fileGrp) {
                    fileGrp = fileSection.newFileGrp()
                    fileSection.addFileGrp(fileGrp)
                    fileGrp.setID(d.key)
                    fileGrp.setUse(use)
                }
                String file_ID = "f" + ++_file_ID;
                def file = fileGrp.newFile()
                fileGrp.addFile(file)
                file.setID(file_ID)
                file.setChecksumType("MD5")
                file.setChecksum(d.value.md5)
                def locat = file.newFLocat()
                file.addFLocat(locat)
                if (doc.master.metadata.pidType) {
                    locat.setLocType("HANDLE")
                    locat.setHref(d.value.metadata.resolverBaseUrl + d.value.metadata.pid + "?locatt=view:" + d.key)
                }
                else {
                    if (d.key == "master")
                        locat.setHref(d.value.metadata.resolverBaseUrl + d.value.metadata.pid)
                    else
                        locat.setHref(grailsApplication.config.grails.serverURL + "/file/" + d.key + "/" + doc.master.metadata.pid)
                }
                locat.setType("simple")

                String group_ID = "g" + _group_ID
                def div = divMain.divs.find {
                    it.ID == group_ID
                }
                if (!div) {
                    div = divMain.newDiv()
                    div.setID(group_ID)
                    div.setOrder(count as String)
                    divMain.addDiv(div)
                }
                final fptr = div.newFptr()
                fptr.setID(file_ID)
                div.addFptr(fptr)
            }
        }

        metsWrapper.write(writer)
    }
}
