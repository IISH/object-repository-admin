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

    def getProperty(String property) {
        if ( property == "id" ) return _id
        if (this.hasProperty(property)) return this.@"$property"
        metadata."$property"
    }

    static List<String> whiteList = [
            'pid',
            'resolverBaseUrl',
            'label',
            'access'
    ]
}
