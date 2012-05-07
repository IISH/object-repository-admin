package org.objectrepository.security

class Bucket {

    String bucket
    String access = "closed"

    static constraints = {
                access(inList: ["open", "restricted", "closed", "deleted"])
    }

    static mapWith = "mongo"
}
