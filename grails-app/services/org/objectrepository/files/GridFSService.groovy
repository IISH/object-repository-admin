package org.objectrepository.files

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MapReduceOutput
import com.mongodb.QueryBuilder
import com.mongodb.gridfs.GridFS
import groovy.xml.StreamingMarkupBuilder
import org.objectrepository.util.OrUtil
import org.springframework.data.mongodb.core.query.Update
import com.mongodb.gridfs.GridFSDBFile

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
        def gridFS = new GridFS(db, bucket)
        gridFS.findOne(queryPidOrLid(pid))
    }

    Orfile findByPidAsOrfile(String pid) {
        if (!pid || pid.isEmpty()) return null
        String na = OrUtil.getNa(pid)
        mongo.getDB(OR + na).getCollection("master.files").findOne(queryPidOrLid(pid))
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

    Orfile parseOrFile(def document) {
        Orfile orfile = null
        if (document instanceof GridFSDBFile) // Sometimes this oddity happens... returns an error: Could not find matching constructor for: org.objectrepository.files.Orfile(com.mongodb.gridfs.GridFSDBFile)
        {
            orfile = new Orfile()
            ["_id", "filename", "contentType", "length", "chunkSize",
                    "uploadDate", "aliases", "md5", "metaData"].each {

            }.each { p ->
                orfile.setProperty(p, document.getProperty(p))
            }
        } else {
            orfile = new Orfile(document)
        }
        orfile
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

    void increment(def file, String bucket) {
        def update = new Update().inc("metadata.timesAccessed", 1).getUpdateObject()
        final collection = mongo.getDB(OR + file.metadata.na).getCollection(bucket + ".files")
        collection.update(_id: file.id, update, false, false)
    }

    Orfile get(String na, String id) {
        mongo.getDB(OR + na).getCollection("master.files").findOne(_id: id)
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
    void writeOrfiles(String id, String na, def writer) {

        def orfileAttributes = [xmlns: "http://objectrepository.org/orfiles/1.0/"]

        def builder = new StreamingMarkupBuilder()
        builder.setEncoding("utf-8")
        builder.setUseDoubleQuotes(true)

        final collection = mongo.getDB(OR + na).getCollection("master.files")
        String comments = String.format('Database contains %s files. Export extracted on %s',
                collection.count(), new Date().toGMTString())
        def cursor = (id) ? collection.find([_id: id]) : collection.find()

        writer << builder.bind {
            mkp.xmlDeclaration()
            comment << comments
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

    def labels(String na) {

        MapReduceOutput output = mongo.getDB(OR + na)."master.files".mapReduce(
                """
        function map() {
            var label = this.metadata.label;
            emit(label, { total:1 });
        }
                """,
                """
        function reduce(key, values) {
            var total = 0;
            var labels = [];
            for (var i = 0; i < values.length; i++) {
                var value = values[i];
                total += value.total;
            }
            return { total:total };
        }
                """,
                "mrlabel",
                [:] // All documents
        )

        output.results().collect {
            [
                    label: it._id,
                    total: it.value.total as Integer
            ]
        }
    }

    def stats(String na) {
        if (!na) na = "0"
        MapReduceOutput output = mongo.getDB(OR + na)."master.files".mapReduce(
                """
        function map() {
            emit(this.contentType, { total:1, length:this.length, timesAccessed:this.metadata.timesAccessed });
        }
        """,
                """
        function reduce(key, values) {
            var total = 0;
            var timesAccessed = 0;
            var length = 0;
            var smallest = 0;
            var largest = 0;
            for (var i = 0; i < values.length; i++) {
                var value = values[i];
                total += value.total;
                timesAccessed += value.timesAccessed;
                length += value.length;
                if (value.length < smallest) smallest = value.length;
                if (value.length > largest) largest = value.length;
            }
            var average = length / total;
            return { total:total, timesAccessed:timesAccessed, length:length, average:average, smallest:smallest, largest:largest };
        }
        """,
                "mrstats",
                [:])

        int length = 0
        int total = 0
        def results = output.results().collect {
            length += it.value.length
            total += it.value.total
            [
                    contentType: it._id,
                    length: it.value.length as Integer,
                    total: it.value.total as Integer,
                    timesAccessed: it.value.timesAccessed as Integer,
                    smallest: it.value.smallest,
                    largest: it.value.largest,
                    average: it.value.average
            ]

        }
        results << [length: length, total: total]
        results
    }
}
