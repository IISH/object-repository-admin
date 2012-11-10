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

    private static String collate = "function() {\n" +
            "            var documents=[];\n" +
            "            db.getCollection('master.files').find(%s).limit(%s).skip(%s).forEach(function(d){\n" +
            "                var cache = [d];\n" +
            "                ['level1.files', 'level2.files', 'level3.files'].forEach(function (c) {\n" +
            "                    if ( d.metadata.pid ){\n" +
            "                        var bucket = db.getCollection(c).findOne({'metadata.pid':d.metadata.pid});\n" +
            "                        if (bucket) {\n" +
            "                            cache.push(\n" +
            "                                bucket\n" +
            "                            )\n" +
            "                        }\n" +
            "                    }\n" +
            "                });\n" +
            "                documents.push(cache)})\n" +
            "            return documents;" +
            "}"

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
        get(na, pid)
    }

    List get(String na, String pid) {
        query(OR + na, String.format("{'metadata.pid':'%s'}", pid))[0]
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
        final q = (params?.label) ? String.format("{'metadata.label': '%s'}", params.label) : ""
        query(OR + na, q, params.max, params.offset)
    }

    int countByNa(def na, def params) {
        final q = (params?.label) ? ['metadata.label': params.label] : ['metadata': [$exists: true]]
        mongo.getDB(OR + na).getCollection("master.files").count(q)
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
                    def documents = get(na, it.metadata.pid)
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
     * note on the nolock setting:
     * There are some circumstances where the eval() implements a strictly-read only operation that need not
     * block other operations when disabling the write lock may be useful. Use this functionality with extreme caution.
     *
     * @param na
     * @return
     */
    def labels(String na) {
        mongo.getDB(OR + na).command([$eval: 'function(){var documents=[];db.label.find().sort({_id: 1}).forEach(function(d){documents.push(d._id)});return documents;}', nolock: true]).retval.plus(0, 'everything')
    }

    /**
     * Returns all technical metadata concerning the PID value.
     *
     * @param db
     * @param query
     * @param limit
     * @param skip
     *
     * note on the nolock setting:
     * There are some circumstances where the eval() implements a strictly-read only operation that need not
     * block other operations when disabling the write lock may be useful. Use this functionality with extreme caution.
     *
     * @return
     */
    private List query(String db, String query, int limit = 1, int skip = 0) {
        mongo.getDB(db).command([$eval: String.format(collate, query, limit, skip), nolock: true]).retval
    }
}
