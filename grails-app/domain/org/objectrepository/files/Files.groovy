package org.objectrepository.files

import org.bson.types.ObjectId

class Files {

    ObjectId id
    String na
    String pid
    String resolverBaseUrl
    String label
    String access = 'closed'
    List<org.objectrepository.domain.File> files

    static embedded = ['files']

    static constraints = {
        resolverBaseUrl(nullable: true)
        label(nullable: true)
    }

    static mapping = {
        database 'or'
        index 'na': true
        index 'pid': true
    }

    static mapWith = "mongo"
}