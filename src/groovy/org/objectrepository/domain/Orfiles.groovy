package org.objectrepository.domain

import com.mongodb.BasicDBObject

class Orfile {

    Object _id
    String filename
    String contentType
    Long length = 0
    int chunkSize
    Date uploadDate
    String[] aliases
    Metadata metadata
    String md5

    String getId() {
        _id
    }

    static List<String> whiteList = [
            'filename',
            'length',
            'md5',
            'contentType',
            'content',
            'firstUploadDate',
            'lastUploadDate',
            'timesUpdated',
            'timesAccessed',
    ]
}

class Metadata {

    String bucket = "Bucket"
    String na
    String pid
    String resolverBaseUrl
    String access
    String fileSet
    String label
    BasicDBObject content
    Date firstUploadDate
    Date lastUploadDate
    int timesAccessed
    int timesUpdated
    List<Orfile> cache
}

