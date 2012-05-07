package org.objectrepository.files

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.util.JSON

class FilesUDService {

    static transactional = false

    /**
     * update
     *
     *  Here we cannot use the Gorm as there is a mismatch in our domain class and what is stored.
     *
     * @param file
     * @return
     */
    def update(String pid, def params) {
        final DBObject query = new BasicDBObject("pid", pid)
        def update = JSON.parse(String.format("{\$set:{label:'%s',access:'%s', resolverBaseUrl:'%s'}}", params.label, params.access, params.resolverBaseUrl))
        Files.collection.update(query, update, true, false)
    }

    /**
     * delete
     *
     * We do not delete a document for now... we just set the access status to 'delete'
     *
     * @param pid
     * @return
     */
    def delete(String pid) {
        final DBObject query = new BasicDBObject("pid", pid)
        def update = JSON.parse("{\$set:{access:'deleted'}}");
        Files.collection.update(query, update, true, false)
    }

    def findOne(String pid, String md5) {
        final DBObject query = new BasicDBObject("pid", pid)
        query.append("md5", md5);
        Files.collection.findOne(query);
    }
}
