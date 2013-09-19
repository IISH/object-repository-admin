package org.objectrepository.security

class UserResource {

    String pid
    Date expirationDate
    int downloadLimit = 0
    int downloads = 0

    static constraints = {
        expirationDate(nullable: true)
    }
}
