package org.objectrepository.files

import com.mongodb.BasicDBObject
import com.mongodb.gridfs.GridFS
import groovy.xml.StreamingMarkupBuilder

import org.objectrepository.util.OrUtil
import org.springframework.data.mongodb.core.query.Update

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
        GridFS gridFS = new GridFS(db, bucket)
        gridFS.findOne(new BasicDBObject("metadata.pid", pid))
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
        mongo.getDB(OR + na).getCollection("master.files").find().limit(params.max).skip(params.offset).collect {
            new Orfile(it)
        }
    }

    int countByNa(def na) {
        mongo.getDB(OR + na).getCollection("master.files").count()
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
                    final Orfile orFile = new Orfile(it)

                    orfile {
                        Orfile.whiteList.each { String key ->
                            out << element(orFile, key)
                            /*if (orFile.hasProperty(key)) {
                                "$key" orFile."$key"
                            } else {
                                "$key" orFile.metadata."$key"
                            }*/
                        }
                        master {
                            Metadata.whiteList.each { String key ->
                                /*if (orFile.hasProperty(key)) {
                                    "$key" orFile."$key"
                                } else {
                                    "$key" orFile.metadata."$key"
                                }*/
                                out << element(orFile, key)
                            }
                        }
                        out << cache(orFile.metadata.cache)
                    }
                }
            }
        }

        writer.flush()
        writer.close()
    }

    private cache(List<Orfile> orfiles) {

        return {
            orfiles.each { def cache ->
                "$cache.metadata.bucket" {
                    Metadata.whiteList.each { String key ->
                        out << element(cache, key)
                    }
                }
/*
                    Metadata.whiteList.each { String key ->
                        def value = (cache."$key") ?: cache.metadata."$key"
                        if (value) {
                            "$key" value
                        }
                }
*/
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
}
