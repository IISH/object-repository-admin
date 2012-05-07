package org.objectrepository.security

class Role {

    String authority

    static constraints = {
        authority blank: false, unique: true
    }

    static mapping = {
        database 'security'
        cache true
    }
    static mapWith = "mongo"
}
