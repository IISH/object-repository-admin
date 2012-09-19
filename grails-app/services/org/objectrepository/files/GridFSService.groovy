package org.objectrepository.files

import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import groovy.xml.StreamingMarkupBuilder
import org.objectrepository.util.OrUtil
import org.springframework.data.mongodb.core.query.Update
import com.mongodb.*

/**
 * GridFSService
 *
 * A lazy file retrieval. Any bucket not loaded, will be at request time.
 *
 */
class GridFSService {

    static String OR = "or_"
    static transactional = false
    def mongo
    def grailsApplication

    /**
     * findByPid
     *
     * Find the document via pid reference.
     *
     * @param pid
     * @return
     */
    def findByPid(String pid, String bucket) {
        if (!pid || pid.isEmpty()) return null
        String na = OrUtil.getNa(pid)
        def db = mongo.getDB(OR + na)
//        db.setReadPreference(ReadPreference.SECONDARY) // defined in DataSource.groovy
        def gridFS = new GridFS(db, bucket)
        gridFS.findOne(queryPidOrLid(pid))
    }

    Orfile findByPidAsOrfile(String pid) {
        if (!pid || pid.isEmpty()) return null
        String na = OrUtil.getNa(pid)
        parseOrFile(mongo.getDB(OR + na).getCollection("master.files").findOne(queryPidOrLid(pid)))
    }

    private static DBObject queryPidOrLid(String pid) {
        QueryBuilder.start().or(new BasicDBObject('metadata.pid', pid), new BasicDBObject("metadata.lid", OrUtil.stripNa(pid))).get()
    }

    /**
     * findAllByNa
     *
     * Find all documents in the collection.
     *
     * @param na
     * @param params for sorting, paging and filtering
     */
    List<Orfile> findAllByNa(def na, def params) {

        // Todo: add sorting
        final query = (params?.label) ? ['metadata.label': params.label] : ['metadata': [$exists: true]]
        mongo.getDB(OR + na).getCollection("master.files").find(query).limit(params.max).skip(params.offset).collect {
            parseOrFile(it)
        }
    }

    int countByNa(def na, def params) {
        final query = (params?.label) ? ['metadata.label': params.label] : ['metadata': [$exists: true]]
        mongo.getDB(OR + na).getCollection("master.files").count(query)
    }

    /**
     * update
     *
     * A unidirectional atomic update
     *
     * @param gridFS
     * @param file
     */
    void update(Orfile orFile, def params) {

        def update = Update.update("metadata.access", params.access).set("metadata.label", params.label).updateObject
        final collection = mongo.getDB(OR + orFile.metadata.na).getCollection("master.files")
        collection.update(_id: orFile.id, update, false, false)
    }

    void siteusage(String na, def document) {
        mongo.getDB(OR + na).'siteusage'.save(document, WriteConcern.NONE)
    }

    Orfile get(String na, String id) {
        parseOrFile(mongo.getDB(OR + na).getCollection("master.files").findOne(_id: id))
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
    void writeOrfiles(def params, String na, def writer) {

        def orfileAttributes = [xmlns: "http://objectrepository.org/orfiles/1.0/"]

        def builder = new StreamingMarkupBuilder()
        builder.setEncoding("utf-8")
        builder.setUseDoubleQuotes(true)

        final collection = mongo.getDB(OR + na).getCollection("master.files")


        def query
        if (params.label) {
            query = ['metadata.label': params.label]
        }
        else {
            query = (params.id) ? [_id: params.id] : ['metadata': [$exists: true]]
        }
        def cursor = collection.find(query)
        writer << builder.bind {
            mkp.xmlDeclaration()
            comment << String.format('Selection contains %s documents. Export extracted on %s',
                    cursor.count(), new Date().toGMTString())
            orfiles(orfileAttributes) {
                cursor.each {
                    final Orfile orFile = parseOrFile(it)
                    orfile {
                        Orfile.whiteList.each { String key ->
                            out << element(orFile, key)
                        }
                        out << metadata(orFile)
                    }
                }
            }
        }

        writer.flush()
        writer.close()
    }

    private metadata(Orfile orFile) {
        return {
            orFile.metadata.cache.each { def cache ->
                final String _resolveUrl = grailsApplication.config.grails.serverURL + "/file/" + cache.metadata.bucket + "/" + cache.metadata.pid
                "$cache.metadata.bucket" {
                    resolveUrl _resolveUrl
                    Metadata.whiteList.each { String key ->
                        out << element(cache, key)
                    }
                }
            }
        }
    }

    private element(def element, String key) {
        return {
            def value = (element."$key") ?: element.metadata."$key"
            if (value) {
                "$key" value
            }
        }
    }

    boolean delete(String pid) {
        // this method is not implemented
        false
    }

    /**
     * labels
     *
     * Presents all collection labels using mapReduce.
     *
     * @param na
     * @return
     */
    def labels(String na) {
        def labels = mongo.getDB(OR + na).'label'.find().collect() { it._id }
        labels.putAt(0, 'everything')
        labels
    }

    private Orfile parseOrFile(def document) {
        Orfile orfile = null
        if (document instanceof GridFSDBFile) // Sometimes this oddity happens... returns an error: Could not find matching constructor for: org.objectrepository.files.Orfile(com.mongodb.gridfs.GridFSDBFile)
        {
            orfile = new Orfile(metadata: new Metadata(document.metaData))
            ["_id", "filename", "contentType", "length", "chunkSize",
                    "uploadDate", "aliases", "md5"].each {

            }.each { p ->
                orfile.setProperty(p, document.get(p))
            }
        } else {
            orfile = new Orfile(document)
        }
        orfile
    }
}
