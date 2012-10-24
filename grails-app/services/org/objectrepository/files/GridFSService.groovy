package org.objectrepository.files

import com.mongodb.BasicDBObject
import com.mongodb.WriteConcern
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import groovy.xml.StreamingMarkupBuilder
import org.objectrepository.util.OrUtil

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

    private static String collate = "function(){" +
            "var cache = [];" +
            "['master.files', 'level1.files', 'level2.files', 'level3.files'].forEach(function (c) {" +
            "    var bucket = db.getCollection(c).findOne({'metadata.pid':'%s'});" +
            "    if (bucket) {" +
            "        cache.push(" +
            "            bucket" +
            "        )" +
            "    }" +
            "});" +
            "return cache;}"

    /**
     * findByPid
     *
     * Find the document via pid reference.
     *
     * @param pid
     * @return
     */
    GridFSDBFile findByPid(String pid, String bucket) {
        if (!pid || pid.isEmpty()) return null
        new GridFS(mongo.getDB(OR + OrUtil.getNa(pid)), bucket).findOne(new BasicDBObject('metadata.pid', pid))
    }

    List findByPidAsOrfile(String pid) {
        if (!pid || pid.isEmpty()) return null
        String na = OrUtil.getNa(pid)
        mongo.getDB(OR + na).command([$eval: String.format(collate, pid, pid, pid), nolock: true]).retval
    }

    /**
     * findAllByNa
     *
     * Find all documents in the collection.
     *
     * @param na
     * @param params for sorting, paging and filtering
     */
    List findAllByNa(def na, def params) {

        final query = (params?.label) ? ['metadata.label': params.label] : ['metadata': [$exists: true]]
        //mongo.getDB(OR + na).getCollection("master.files").find(query, ['metadata.pid': 1]).limit(params.max).skip(params.offset).collect { BasicDBObject it ->  // casts to GridFSDBFile
        mongo.getDB(OR + na).getCollection("master.files").find(query).limit(params.max).skip(params.offset).collect {
            mongo.getDB(OR + na).command([$eval: String.format(collate, it.metadata.pid, it.metadata.pid, it.metadata.pid), nolock: true]).retval
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
    void update(def document) {

        mongo.getDB(OR + document.metadata.na).getCollection("master.files").update([_id: document._id],
                [$set: ['metadata.access': document.metadata.access, 'metadata.label': document.metadata.label]], false, false)
    }

    void siteusage(String na, def document) {
        mongo.getDB(OR + na).'siteusage'.save(document, WriteConcern.NONE)
    }

    List get(String na, String pid) {
        mongo.getDB(OR + na).command([$eval: String.format(collate, pid, pid, pid), nolock: true]).retval
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

        def cursor
        int count = 0
        if (params.pid) {
            cursor = [[metadata: [pid: params.pid]]] // should be the same map as returned by a cursor
            count = 0
        } else {
            cursor = (params.label?.length() == 0) ? collection.find([:], ['metadata.pid': 1]) : collection.find(['metadata.label': params.label], ['metadata.pid': 1])
            count = cursor.count()
        }

        writer << builder.bind {
            mkp.xmlDeclaration()
            comment << String.format('Selection contains %s documents. Export extracted on %s',
                    count, new Date().toGMTString())
            orfiles(orfileAttributes) {
                cursor.each {
                    final String p = it.metadata.pid
                    def documents = collection.getDB().command([$eval: String.format(collate, p, p, p), nolock: true]).retval
                    def master = documents[0]
                    if (master)
                        orfile {
                            pid master.metadata.pid
                            resolverBaseUrl master.metadata.resolverBaseUrl
                            pidurl master.metadata.resolverBaseUrl + master.metadata.pid
                            if (master.metadata.lid) { lid master.metadata.lid }
                            filename master.filename
                            label master.metadata.label
                            access master.metadata.access
                            out << metadata(documents, master)
                        }
                }
            }
        }

        writer.flush()
        writer.close()
    }

    private metadata(def documents, def master) {
        return {
            documents.each { document ->
                final String _resolveUrl = grailsApplication.config.grails.serverURL + "/file/" + document.metadata.bucket + "/" + document.metadata.pid
                "$document.metadata.bucket" {
                    if (master.metadata.pidType) {
                        pidurl document.metadata.resolverBaseUrl + document.metadata.pid + "?locatt=view:" + document.metadata.bucket
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
                        if (document[it]) {
                            "$it" document[it]
                        } else if (document.metadata[it]) {
                            "$it" document.metadata[it]
                        }
                    }
                }

            }
        }
    }

    /**
     * labels
     *
     * Presents all collection labels.
     *
     * @param na
     * @return
     */
    def labels(String na) {
        mongo.getDB(OR + na).'label'.find().sort([_id: 1]).collect() { it._id }.plus(0, 'everything')
    }
}
