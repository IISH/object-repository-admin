package org.objectrepository.files

class Orfile {

    Object _id
    String filename
    String contentType
    Long length = 0
    int chunkSize
    Date uploadDate
    String[] aliases
    Metadata metadata
    String md5

    String getId() {
        _id
    }

    static List<String> whiteList = [
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
