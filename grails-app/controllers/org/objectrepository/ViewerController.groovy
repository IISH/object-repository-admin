package org.objectrepository

class ViewerController {

    def index() {

        if (params.pid) {
            params.type = (params.type) ?: 'video/mp4'
            params.poster = (params.poster) ?: 'level2'
            params.source = (params.source) ?: 'level1'
        }
    }
}
