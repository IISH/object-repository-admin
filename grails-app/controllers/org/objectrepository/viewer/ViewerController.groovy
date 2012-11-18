package org.objectrepository.viewer

class ViewerController {

    def index() {

        // What are we ?

        params.type = (params.type) ?: 'video/mp4'
        params.poster = (params.poster) ?: grailsApplication.config.grails.serverURL + '/file/level2/' + params.pid
        params.source = (params.source) ?: grailsApplication.config.grails.serverURL + '/file/level1/' + params.pid
        params.width = (params.width) ?: 640
        params.height = (params.height) ?: 264
    }
}
