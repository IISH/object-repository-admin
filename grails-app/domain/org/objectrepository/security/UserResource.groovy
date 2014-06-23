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
        pid(matches:'^.+\\/.+')
        objid(nullable: true)
        contentType(nullable: true)
        expirationDate(nullable: true)
        downloadLimit(min: 0)
        downloads(min: 0)
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

    void expand() {
        folders = folders.inject([]) { acc, folder ->
            expandFolders(acc, folder)
        }
    }

    /**
     * locations
     *
     * Split the location element
     *
     * @param list
     * @param l
     */
    private static def expandFolders(def list, String l) {
        String s = ""
        l.split('/')[1..-1].each {
            s += '/' + it
            if (!(s in list))
                list << s
        }
        list
    }

    UserResource me(boolean clone) {
        (clone) ? new UserResource(
                pid: this.pid,
                objid: this.objid,
                contentType: this.contentType,
                thumbnail: this.thumbnail,
                expirationDate: this.expirationDate,
                downloadLimit: this.downloadLimit,
                httpDownloads: this.httpDownloads,
                ftpDownloads: this.ftpDownloads,
                buckets: this.buckets,
                folders: this.folders
        ) : this
    }

}
