package org.objectrepository.files

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.WriteConcern
import com.mongodb.gridfs.GridFS
import com.mongodb.util.JSON

import org.objectrepository.util.OrUtil
import org.objectrepository.domain.Orfiles

/**
 * GridFSService
 *
 * A lazy file retrieval. Any bucket not loaded, will be at request time.
 *
 */
class GridFSService {

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
        def db = mongo.getDB("or_" + na)
        GridFS gridFS = new GridFS(db, bucket)
        gridFS.findOne(new BasicDBObject("metadata.pid", pid))
    }

    /**
     * findAllByNa
     *
     * Find all documents via na reference
     *
     * @param na
     * @param params for sorting, paging and filtering
     */
    List<Orfiles> findAllByNa(def na, def params) {
        def collection = mongo.getDB("or_" + na).getCollection("master.files")
        collection.find('metadata.na': na).limit(params.max).skip(0).collect { // may dd .sort(key:1)
            new Orfiles(it)
        }
    }

    /**
     * update
     *
     * As we cannot perform a FindAndModify using GridFS, we need a unidirectional atomic update
     *
     * @param gridFS
     * @param file
     */
    void update(def file, String bucket) {
        final DBObject query = new BasicDBObject('_id', file._id)
        final DBObject update = (DBObject) JSON.parse("{\$inc:{'metadata.timesAccessed':1}}")
        final def db = mongo.getDB("or_" + file.metadata.na)
        db.getCollection(bucket + ".files").update(query, update, false, false, WriteConcern.NONE)
    }

    Orfiles get(String na, String id) {
        mongo.getDB("or_" + na).getCollection("master.files").findOne(_id: id)
    }
}
