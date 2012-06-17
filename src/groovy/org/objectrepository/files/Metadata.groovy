package org.objectrepository.files

import com.mongodb.BasicDBObject

class Metadata {

    String bucket = "Bucket"
    String na
    String pid
    String lid
    String resolverBaseUrl
    String pidUrl
    String pidType
    String access
    String fileSet
    String label
    BasicDBObject content
    Date firstUploadDate
    Date lastUploadDate
    int timesAccessed
    int timesUpdated
    List<Orfile> cache

    static List<String> whiteList = [
            'pidUrl',
            'filename',
            'length',
            'md5',
            'contentType',
            'content',
            'firstUploadDate',
            'lastUploadDate',
            'timesUpdated',
            'timesAccessed'
    ]
}