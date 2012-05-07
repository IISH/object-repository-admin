package org.objectrepository.files

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import com.mongodb.util.JSON
import org.objectrepository.domain.File
import groovy.transform.Synchronized

/**
 * GridFSService
 *
 * A lazy file retrieval. Any bucket not loaded, will be at request time.
 *
 */
class GridFSService {

    static transactional = false

    Map<String, GridFS> gridFS = [:]

    GridFSDBFile getFile(File file) {

        GridFS fs = getGridFS(file.bucket)
        update(fs, file)
        fs.findOne(file.objectId)
    }

    /**
     * update
     *
     * As we cannot perform a FindAndModify using GridFS, we need a unidirectional update
     *
     * @param gridFS
     * @param file
     */
    private void update(GridFS gridFS, File file) {
        DBObject query = new BasicDBObject('files.file.\$id', file.objectId)
        DBObject update = JSON.parse("{\$inc:{'files.\$.timesAccessed':1}}")
        gridFS.getDB().getCollection('files').update(query, update, false, false)
    }

    @Synchronized
    def loadGridFS(String bucket) {
        GridFS fs = gridFS[bucket] // double fail safe singleton
        if (fs == null) {
            fs = new GridFS(Files.collection.DB, bucket)
            gridFS.put(bucket, fs)
        }
        fs
    }

    def getGridFS(String bucket) {
        def fs = gridFS[bucket]
        if (!fs) {
            fs = loadGridFS(bucket)
        }
        fs
    }
}
