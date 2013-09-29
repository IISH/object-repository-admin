package org.objectrepository.security

class UserResource {

    String pid
    boolean objid = false
    String contentType
    boolean thumbnail = true
    Date expirationDate
    int downloadLimit = 0
    int downloads = 0
    String[] buckets = ['master']

    static constraints = {
        contentType(nullable: true)
        expirationDate(nullable: true)
        downloadLimit(min: 0)
        downloads(min: 0)
        buckets(inList:['master', 'level1', 'level2', 'level3'])
    }

    static mapping = {
        version false
        pid attr: 'p'
        objid attr: 'c'
        contentType attr: 'ct'
        thumbnail attr: 't'
        expirationDate attr: 'e'
        downloadLimit attr: 'l'
        downloads attr: 'd'
        buckets attr: 'b'
    }
}
