package org.objectrepository.mets

import au.edu.apsr.mtk.base.Div
import au.edu.apsr.mtk.base.METSWrapper
import com.mongodb.DBCursor
import au.edu.apsr.mtk.ch.METSReader

class MetsService {

    static transactional = false
    static String OR = "or_"
    static def uses = [master: 'archive', level1: 'hires reference', level2: 'reference', level3: 'thumbnail']
    def mongo
    def grailsApplication
    def gridFSService

    METSWrapper writeMetsFile(String na, String labelOrPID = null, boolean cache = true) {

        METSWrapper wrapper

        final filename = na + "_" + labelOrPID + ".xml"
        final File file = new File(System.getProperty("java.io.tmpdir"), filename)
        long expire = new Date().minus(1).time
        if (cache && file.exists() && file.lastModified() > expire) {
            log.info "Using cache " + file.absolutePath
            def mr = new METSReader()
            final stream = new FileInputStream(file)
            mr.mapToDOM(stream)
            stream.close()
            final document = mr.getMETSDocument()
            // A bug in the METSReader introduces a second identical xmlns. We move the first
            final attribute = document.getDocumentElement().getAttributeNode("xmlns")
            document.getDocumentElement().removeAttributeNode(attribute)
            return new METSWrapper(document)
        }

        if (labelOrPID) {
            final collection = mongo.getDB(OR + na).getCollection("master.files")
            def doc = collection
                    .findOne([$or: [['metadata.pid': na + "/" + labelOrPID], ['metadata.label': labelOrPID]]],
                    ['metadata.fileSet': 1, 'metadata.label': 1])
            wrapper = (doc) ? metsFile(na, doc?.metadata?.label, doc?.metadata?.fileSet) : null
        } else wrapper = writeRootMetsFile(na, gridFSService.labels(na))

        final fos = new FileOutputStream(file, false)
        wrapper.write(fos)
        fos.close()
        wrapper
    }

    /*
    The main mets fileSec just a list of labels \ virtual folders
     */

    private writeRootMetsFile(def na, def labels) {

        final def metsWrapper = new METSWrapper()
        final mets = metsWrapper.getMETSObject()
        final fileSection = mets.newFileSec()
        mets.setFileSec(fileSection)
        final fileGrp = fileSection.newFileGrp()
        fileSection.addFileGrp(fileGrp)
        fileGrp.setID("g0")
        fileGrp.setUse("folder")

        final logicalMap = mets.newStructMap()
        mets.addStructMap(logicalMap)
        logicalMap.setType("logical")

        int _file_ID = 0
        labels.each {
            String file_ID = "f" + ++_file_ID
            def file = fileGrp.newFile()
            fileGrp.addFile(file)
            file.setID(file_ID)

            def locat = file.newFLocat()
            file.addFLocat(locat)
            locat.setTitle(it)
            locat.setHref(grailsApplication.config.grails.serverURL + "/mets/" + na + "/" + it)

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
     *  - collect the documents by label   or PID value
     *  - the grouping will be according to foldername of the targeted query: label=all files in the fileSet; pid=all files sharing the same folder of the file
     *  - for each found level, produce a fileSec.
     *  - reconstruct a filesystem view based on the filename:
     *  /
     *  /label here/
     *  /label here/bucket name/
     *  /label here/bucket name/folder name(and subfolders)
     *  /label here/bucket name/folder name(and subfolders)/filename
     */
    private METSWrapper metsFile(String na, String label, String location) {// todo: look in location element: /a/b/c/d/e/filename.tif

        final DBCursor cursor = mongo.getDB(OR + na).getCollection("master.files")
                .find([$and: [
                ['metadata.label': label],
                ['metadata.fileSet': [$regex: '^' + location, $options: 'i']]]]
                , ['metadata.pid': 1])
        println(cursor.count())
        println(label)
        if (cursor.count() == 0) return

        final def metsWrapper = new METSWrapper()
        final mets = metsWrapper.getMETSObject()
        final fileSection = mets.newFileSec()
        mets.setFileSec(fileSection)

        final physicalMap = mets.newStructMap()
        mets.addStructMap(physicalMap)
        physicalMap.setType("physical");
        final divMainPhysical = physicalMap.newDiv()
        physicalMap.addDiv(divMainPhysical)


        final logicalMap = mets.newStructMap()
        mets.addStructMap(logicalMap)
        logicalMap.setType("logical")
        final divMainLogical = logicalMap.newDiv()
        logicalMap.addDiv(divMainLogical)

        int count = 0
        int _file_ID = 0
        int _group_ID = 0
        int i = 0
        final ids = [:]

        label = "/" + label.replace("/", "_")
        divMainLogical.setType("root")
        divMainLogical.setID('g0')
        divMainLogical.setLabel(label)
        ids << ['g0': label]

        // Folders always end with a /
        while (cursor.hasNext()) {
            def doc = gridFSService.get(na, cursor.next().metadata.pid)
            _group_ID++
            doc.each { def d ->
                final String folder = label + "/" + d.key + doc.master.metadata.fileSet // /a/b/c/d. No trailing slash
                log.info "folder=" + folder
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
                file.setSize(d.value.length)
                file.setMIMEType(d.value.contentType)
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
                locat.setTitle(d.value.filename)

                final StringBuilder sb = new StringBuilder()
                def logical_div
                folder.substring(1).split("/").each() {
                    sb.append("/" + it)
                    def ID = ids.find {
                        it.value == sb.toString()
                    }
                    if (ID) {
                        logical_div = findDiv(divMainLogical, ID.key)
                    } else {
                        ID = "g" + ++i
                        ids.put(ID, sb.toString())
                        println("ID:" + ID + "; value " + sb.toString())
                        String parentValue = sb.substring(0, sb.lastIndexOf("/"))
                        println("parentValue='" + parentValue + "'")
                        def parent = ids.find {
                            it.value == parentValue
                        }
                        println("parent=" + parent.key)
                        def parentDiv = findDiv(divMainLogical, parent.key)
                        logical_div = parentDiv.newDiv()
                        logical_div.setID(ID)
                        logical_div.setLabel(sb.toString())
                        parentDiv.addDiv(logical_div)
                        println("Total size parent logical_div: " + parentDiv.divs.size())
                    }
                }
                final div = addPhysicalDiv(divMainPhysical, ++count, _group_ID)
                addFptrToDiv(div, file_ID)
                addFptrToDiv(logical_div, file_ID)
            }
        }

        metsWrapper
    }

    private def addPhysicalDiv(def div, int count, int _group_ID) {
        String physical_group_ID = "g" + _group_ID
        def physical_div = div.divs.find {
            it.ID == physical_group_ID
        }
        if (!physical_div) {
            physical_div = div.newDiv()
            physical_div.setID(physical_group_ID)
            physical_div.setOrder(count as String)
            div.addDiv(physical_div)
        }
        physical_div
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