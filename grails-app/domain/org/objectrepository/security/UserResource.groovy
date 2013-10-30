package org.objectrepository.security

class UserResource {

    String pid
    String objid
    String contentType
    boolean thumbnail = true
    Date expirationDate
    int downloadLimit = 0
    int httpDownloads = 0
    int ftpDownloads = 0
    List<String> buckets = ['master']
    List<String> folders = []

    int getDownloads() {
        httpDownloads + ftpDownloads
    }

    void setDownloads(int d) {
        httpDownloads = ftpDownloads = d
    }

    static constraints = {
        objid(nullable: true)
        contentType(nullable: true)
        expirationDate(nullable: true)
        downloadLimit(min: 0)
        downloads(min: 0)
        buckets(inList: ['master', 'level1', 'level2', 'level3'])
    }

    static mapping = {
        version false
        pid attr: 'p'
        objid attr: 'c'
        contentType attr: 'ct'
        thumbnail attr: 't'
        expirationDate attr: 'e'
        downloadLimit attr: 'l'
        httpDownloads attr: 'hd'
        ftpDownloads attr: 'fd'
        folders attr: 'f'
        buckets attr: 'b'
    }

}
