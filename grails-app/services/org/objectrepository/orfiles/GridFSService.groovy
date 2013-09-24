package org.objectrepository.orfiles

import com.mongodb.BasicDBObject
import com.mongodb.WriteConcern
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import org.objectrepository.security.User
import org.objectrepository.security.UserResource
import org.objectrepository.util.OrUtil

import javax.servlet.http.HttpServletResponse

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
            "var l = [];" +
            "db.getCollection('master.files').find(%s).limit(%s).skip(%s).sort({'metadata.objid':1,'metadata.seq':1}).forEach(function(m){" +
            "   if ( m.metadata.pid ){" +
            "       var f={master:m};" +
            "       ['level1', 'level2', 'level3'].forEach(function (d) {" +
            "           var bucket = db.getCollection(d+'.files').findOne({'metadata.pid':m.metadata.pid});" +
            "           if (bucket) f[d]=bucket;" +
            "       });" +
            "       l.push(f);" +
            "   }" +
            "});" +
            "return l;" +
            "}"

    /**
     * findByPid
     *
     * Find the document via pid reference.
     *
     * @param pid
     * @return
     */
    GridFSDBFile findByPid(String pid, String bucket = 'master') {
        if (bucket in grailsApplication.config.buckets && pid) new GridFS(mongo.getDB(OR + OrUtil.getNa(pid)), bucket).findOne(new BasicDBObject('metadata.pid', pid))
    }

    GridFSDBFile findByField(String na, String bucket, String key, String value) {
        new GridFS(mongo.getDB(OR + na), bucket).findOne(new BasicDBObject(key, value))
    }

    def findByPidAsOrfile(String pid) {
        if (!pid || pid.isEmpty()) return null
        String na = OrUtil.getNa(pid)
        get(na, pid)
    }

    def get(String na, String pid) {
        query(OR + na, String.format("{\$or:[{'metadata.pid':'%s'},{'metadata.objid':'%s', 'metadata.seq':1}]}", pid, pid))[0]
    }

    /**
     * findAllByLabel
     *
     * Find all documents in the collection.
     *
     * @param na
     * @param params for sorting, paging and filtering
     */
    def findAllByLabel(def na, def params) {
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

        grailsApplication.config.buckets.each {
            mongo.getDB(OR + document.metadata.na).getCollection(it + ".files").update(['metadata.pid': document.metadata.pid],
                    [$set: ['metadata.access': document.metadata.access,
                            'metadata.embargo': document.metadata.embargo,
                            'metadata.embargoAccess': document.metadata.embargoAccess,
                            'metadata.label': document.metadata.label,
                            'metadata.objid': document.metadata.objid,
                            'metadata.seq': document.metadata.seq]], false, false)
        }
    }

    void siteusage(String na, def document) {
        if ( grailsApplication.config.siteusage )
            mongo.getDB(OR + na).'siteusage'.save(document, WriteConcern.NONE)
    }

    /*void download(User user, UserResource resource) {
        mongo.getDB('security').user.update(
                [_id:user.id,'resource.pid':resource.pid],
                [$set:['resources.$.downloads':user.resources]], false, false, WriteConcern.NONE)
    }*/

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
        mongo.getDB(OR + na).command([$eval: 'function(){var documents=[];db.label.find().sort({_id: 1}).forEach(function(d){documents.push(d._id)});return documents;}', nolock: true]).retval
    }

/**
 * Returns all technical metadata concerning the PID orfileInstance.
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
    private def query(String db, String query, int limit = 1, int skip = 0) {
        final command = mongo.getDB(db).command([$eval: String.format(collate, query, limit, skip), nolock: true])
        command.retval
    }

/**
 * skip
 *
 * Derived from the GridFSDBFile class.
 *
 * Locates the chunks that over the specified range bytes=n-m
 */
    public int range(OutputStream writer, GridFSDBFile file, long from, long to) {

        if (from > to) return HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE

        // find first chunk by index:
        int fromChunk = Math.floor(from / file.chunkSize)
        if (fromChunk < 0 || fromChunk > file.numChunks() - 1) return HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE

        // find the last chunk by index:
        int toChunk = Math.floor(to / file.chunkSize)
        if (toChunk < 0 || toChunk > file.numChunks() - 1) return HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE

        // Standardize to the range of chunkSize
        from = from % file.chunkSize
        to = to % file.chunkSize

        long c = 0
        int fromOffset, toOffset, length
        // now get all chunks that cover this range:
        for (int n = fromChunk; n <= toChunk; n++) {
            fromOffset = (fromChunk == n) ? from : 0
            toOffset = (toChunk == n) ? to : file.chunkSize - 1
            length = toOffset - fromOffset + 1
            c += length
            final byte[] _data = file.getChunk(n)
            if (log.isInfoEnabled()) println([chunk: n, fromChunk: fromChunk, toChunk: toChunk, from: from, to: to, fromOffset: fromOffset, toOffset: toOffset, _dataLength: _data.length, length: length, total: c, fileLength: file.length])
            writer.write(_data, fromOffset, length)
        }

        -1
    }

    /**
     * vfs
     *
     * The base name of the folder is always the database name.
     * /a/b/c/d => na=a
     *
     * Convention:
     * a colon in the na delimits a user
     * /a:b/c/d/e => na = a
     *
     * @param currentFolder The path. For example: /a/b/c/d
     * @return
     */
    def vfs(String currentFolder) {

        String na = currentFolder.split('/')[1].split(':')[0]
        final db = mongo.getDB(OR + na)
        db.vfs.findOne([_id: currentFolder])
    }

    /**
     * objid
     *
     * Lists all objid for the na
     *
     * @param na
     * @return
     */
    def objid(String na) {
        mongo.getDB(OR + na).'master.files'.find(['metadata.objid': [$exists: true], 'metadata.seq': 1]).collect {
            it.metadata.objid
        }
    }

    def listFilesByObjid(String na, String bucket, String id) {
        new GridFS(mongo.getDB(OR + na), bucket)
                .find(new BasicDBObject('metadata.objid', na + '/' + id))
                .sort { it.metaData.seq }
    }

    def listFilesByLabel(String na, String bucket = 'master', String label) {
        new GridFS(mongo.getDB(OR + na), bucket)
                .find(new BasicDBObject('metadata.label', label))
                .sort { it.metadata.seq }
    }

    /**
     * countPidOrObjId
     *
     * Counts all master files with the given pid or objid
     * Returns at least one level3 instance for preview
     *
     * @param na
     * @param id
     * @return
     */
    def countPidOrObjId(String na, String id) {
        final q = [$or: [['metadata.objid': id], ['metadata.pid': id]]]
        int count = mongo.getDB(OR + na).'master.files'.count(q)
        def orfile = ( count ) ? get(na, id) : null
        [count:count, orfile:orfile]
    }
}
