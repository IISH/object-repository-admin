package org.objectrepository.files

import com.mongodb.BasicDBObject

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