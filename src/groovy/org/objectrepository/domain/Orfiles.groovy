package org.objectrepository.domain

import com.mongodb.BasicDBObject

class Orfiles {

    Object _id
    String filename
    String contentType
    Long length = 0
    int chunkSize
    Date uploadDate
    String[] aliases
    Metadata metadata
    String md5

    String bucket // data should go under metadata

    String getId() {
        _id
    }
}

class Metadata {
    String bucket
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
    List<Orfiles> files
}

