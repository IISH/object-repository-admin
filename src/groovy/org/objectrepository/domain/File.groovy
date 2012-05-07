package org.objectrepository.domain

import com.mongodb.BasicDBObject
import org.bson.types.ObjectId

class File extends FileBase {

    String bucket
    int timesAccessed = 0
    int timesUpdated = 0
    Date firstUploadDate = new Date()
    Date lastUploadDate = new Date()
    BasicDBObject metadata
    String[] file    // dbref does not work yet... in this way. This value will only hold the _id

    protected ObjectId getObjectId() {
        new ObjectId(file[0])
    }

    protected String getTaskId(){
        metadata['taskId']
    }
}


