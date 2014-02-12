package org.objectrepository.mets

import groovy.xml.StreamingMarkupBuilder
import org.objectrepository.util.OrUtil

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
    def policyService
    
    /**
     * countMetsFile
     *
     * @param na The naming authority
     * @param objid Compound object
     * @return
     */
    long countMetsFile(String na, String objid) {
        gridFSService.countFilesByObjid(na, 'master', na + '/' + objid)
    }

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
    String writeMetsFile(String na, String objid, def buckets = grailsApplication.config.buckets) {

        if (objid)
            metsFile(na, buckets, objid)
        else
            null
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
    private String metsFile(String na, def buckets, String objid) {

        def levels = buckets.inject([:]) { map, bucket ->
            def list = gridFSService.listFilesByObjid(na, bucket, objid)
            if (list.size()) map[bucket] = list
            map
        }

        if (!levels) return null

        int _file_ID = 0
        def map = [:]

        def fileSec = {
            fileSec {
                levels.each {
                    def bucket = it.key
                    def listOfFiles = it.value
                    String type = listOfFiles[0].contentType.split('/')[0]
                    String use = uses[bucket] + " " + type
                    fileGrp(ID: bucket, USE: use) {
                        listOfFiles.each { d ->
                            String file_ID = "f" + ++_file_ID;
                            file(
                                    CHECKSUM: d.md5,
                                    CHECKSUMTYPE: 'MD5',
                                    CREATED: d.uploadDate.format("yyyy-MM-dd'T'mm:hh:ss'Z'"),
                                    ID: file_ID,
                                    MIMETYPE: d.contentType,
                                    SIZE: d.length
                            ) {
                                String _LOCTYPE = (d.metadata.pidType == 'or') ? 'HANDLE' : 'URL'
                                String href = (d.metadata.pidType == 'or') ? d.metadata.resolverBaseUrl + d.metadata.pid + "?locatt=view:" + bucket : d.metadata.resolverBaseUrl + d.metadata.pid
                                FLocat(
                                        LOCTYPE: _LOCTYPE,
                                        'xlink:href': href,
                                        'xlink:title': d.filename,
                                        'xlink:type': 'simple'
                                )
                            }

                            if (bucket == 'master') map[d.metadata.pid] = []
                            map[d.metadata.pid] << [file_ID: file_ID, seq: d.metadata.seq as Integer]
                        }
                    }
                }
            }
        }

        def structMap = {
            structMap(TYPE: 'physical') {
                div {
                    map.eachWithIndex { val, i ->
                        div(
                                ID: "g" + i,
                                LABEL: "Page " + (i + 1),
                                ORDER: val.value[0].seq as String,
                                TYPE: 'page'
                        ) {
                            val.value.each {
                                fptr(FILEID: it.file_ID)
                            }
                        }
                    }
                }
            }
        }

        def amdSec = {
            amdSec(ID: 'admSec-1') {
                rightsMD(ID: 'rightsMD-1') {
                    mdWrap(MDTYPE: 'OTHER') {
                        xmlData {
                            'epdcx:descriptionSet'('xmlns:epdcx': 'http://purl.org/eprint/epdcx/2006-11-16/', 'xsi:schemaLocation': 'http://purl.org/eprint/epdcx/2006-11-16/ http://purl.org/eprint/epdcx/xsd/2006-11-16/epdcx.xsd') {
                                levels.each {
                                    def resourceId = it.key
                                    def listOfFiles = it.value
                                    String access = listOfFiles[0].metadata.access
                                    String valueRef = "http://purl.org/eprint/accessRights/" + OrUtil.camelCase([policyService._getPolicy(na, access).getAccessForBucket(resourceId), "Access"])
                                    'epdcx:description'('epdcx:resourceId': resourceId) {
                                        'epdcx:statement'('epdcx:propertyURI': 'http://purl.org/dc/terms/available', 'epdcx:valueRef': valueRef) {
                                            if (listOfFiles[0].metadata.embargo?.length() == 10)
                                                'epdcx:valueString'('epdcx:sesURI': 'http://purl.org/dc/terms/W3CDTF', listOfFiles[0].metadata.embargo)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        def mets = {
            mets(
                    xmlns: 'http://www.loc.gov/METS/',
                    'xmlns:xlink': 'http://www.w3.org/1999/xlink',
                    'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                    'xsi:schemaLocation': 'http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd',
                    OBJID: na + '/' + objid) {
                out << amdSec
                out << fileSec
                out << structMap
            }
        }

        new StreamingMarkupBuilder().bind(mets)
    }
}
