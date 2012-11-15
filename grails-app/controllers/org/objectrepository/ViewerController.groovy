package org.objectrepository

class ViewerController {

    def index() {

        params.type = (params.type) ?: 'video/mp4'
    }
}
