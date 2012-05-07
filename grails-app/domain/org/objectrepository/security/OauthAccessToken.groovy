package org.objectrepository.security

class OauthAccessToken {

    static mapping = {
        database 'security'
        collection 'oauth_access_token'
    }

    static mapWith = "mongo"
}
