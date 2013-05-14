package org.objectrepository.mets

import au.edu.apsr.mtk.base.Div
import au.edu.apsr.mtk.base.METSWrapper

/**
 * MetsService
 *
 * Offer a view to the stored files:
 * - physical: the files are grouped by bucket and in order of sequence (if any)
 */
class MetsService {

    static transactional = false
    static def uses = ['master': 'archive', 'level1': 'hires reference', 'level2': 'reference', 'level3': 'thumbnail']
    def grailsApplication
    def gridFSService

    /**
     * writeMetsFile
     *
     * Returns an XML object in its wrapper
     *
     * @param na
     * @param objidOrPidOrLabel
     * @param cache
     * @return
     */
    METSWrapper writeMetsFile(String na, String objid, def buckets = ['master', 'level1', 'level2', 'level3']) {

        if (objid)
            metsFile(na, buckets, objid)
        else
            writeRootMetsFile(na, objid)
    }

    /**
     *      The main mets fileSec just a list of objids \ virtual folders
     * @param na
     * @return
     */
    private writeRootMetsFile(def na, def objid) {

        def objids = gridFSService.objid(na)

        final def metsWrapper = new METSWrapper()
        final mets = metsWrapper.getMETSObject()
        mets.setObjID(na + '/' + objid)
        final fileSection = mets.newFileSec()
        mets.setFileSec(fileSection)
        final fileGrp = fileSection.newFileGrp()
        fileSection.addFileGrp(fileGrp)
        fileGrp.setID("g0")
        fileGrp.setUse("mets")

        final logicalMap = mets.newStructMap()
        mets.addStructMap(logicalMap)
        logicalMap.setType("logical")

        int _file_ID = 0
        objids.each {
            String file_ID = "f" + ++_file_ID
            def file = fileGrp.newFile()
            fileGrp.addFile(file)
            file.setID(file_ID)

            def locat = file.newFLocat()
            file.addFLocat(locat)
            locat.setHref("http://hdl.handle.net/$it")

            def div = logicalMap.newDiv()
            div.setLabel("/" + it)
            logicalMap.addDiv(div)
            addFptrToDiv(div, file_ID)
        }
        metsWrapper
    }

    /**
     * writeMetsFile
     *
     * Output the METS object to the writer.
     *
     * Strategy:
     *  - use the corresponding objid or fileSet\label combination to get a sorted list. Sorting is by seq
     *  - for each found level, produce a fileSec.
     */
    private METSWrapper metsFile(String na, def buckets, String objid) {

        final def metsWrapper = new METSWrapper()
        final mets = metsWrapper.getMETSObject()
        mets.setObjID(na + '/' + objid)
        final fileSection = mets.newFileSec()
        mets.setFileSec(fileSection)

        final physicalMap = mets.newStructMap()
        mets.addStructMap(physicalMap)
        physicalMap.setType("physical");
        final divMainPhysical = physicalMap.newDiv()
        physicalMap.addDiv(divMainPhysical)

        int _file_ID = 0
        def map = [:]
        buckets.each { bucket ->
            gridFSService.listFilesByObjid(na, bucket, objid).each { d ->

                String type = d.contentType.split('/')[0]
                String use = uses[bucket] + " " + type

                def fileGrps = fileSection.getFileGrpByUse(use)
                def fileGrp = (fileGrps.size() == 0) ? null : fileGrps[0]
                if (!fileGrp) {
                    fileGrp = fileSection.newFileGrp()
                    fileSection.addFileGrp(fileGrp)
                    fileGrp.setID(bucket)
                    fileGrp.setUse(use)
                }

                String file_ID = "f" + ++_file_ID;
                def file = fileGrp.newFile()
                fileGrp.addFile(file)
                file.setID(file_ID)
                file.setChecksumType("MD5")
                file.setChecksum(d.md5)
                file.setSize(d.length)
                file.setMIMEType(d.contentType)
                file.setCreated(d.uploadDate.format("yyyy-MM-dd'T'mm:hh:ss'Z'"))
                def locat = file.newFLocat()
                file.addFLocat(locat)
                if (d.metadata.pidType == 'or') {
                    locat.setLocType("HANDLE")
                    locat.setHref(d.metadata.resolverBaseUrl + d.metadata.pid + "?locatt=view:" + bucket)
                }
                else {
                    locat.setHref(d.metadata.resolverBaseUrl + d.metadata.pid)
                }
                locat.setType("simple")
                locat.setTitle(d.filename)

                if (bucket == 'master') map[d.metadata.pid] = []
                map[d.metadata.pid] << [file_ID: file_ID, seq: d.metadata.seq as Integer]
            }
        }

        map.eachWithIndex { val, i ->
            def div = divMainPhysical.newDiv()
            div.setID("g" + i)
            div.setOrder(val.value[0].seq as String)
            div.setType("page")
            div.setLabel("Page " + (i + 1))
            divMainPhysical.addDiv(div)
            val.value.each {
                addFptrToDiv(div, it.file_ID)
            }
        }

        metsWrapper
    }

    private void addFptrToDiv(Div div, String ID) {
        final fptr = div.newFptr()
        fptr.setFileID(ID)
        div.addFptr(fptr)
    }

    private Div findDiv(Div div, String ID) {
        if (div.ID == ID) return div;
        for (int i = 0; i < div.divs.size(); i++) {
            def d = findDiv(div.divs[i], ID)
            if (d) return d;
        }
        null
    }
}