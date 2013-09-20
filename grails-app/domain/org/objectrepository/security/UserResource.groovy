package org.objectrepository.security

class UserResource {

    String pid
    Date expirationDate
    int downloadLimit = 0
    int downloads = 0
    int interval = 1

    static constraints = {
        expirationDate(nullable: true)
        downloadLimit(min: 0)
        downloads(min: 0)
        interval(min: 1)
    }

}
