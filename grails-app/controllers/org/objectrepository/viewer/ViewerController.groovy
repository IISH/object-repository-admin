package org.objectrepository.viewer

/**
 * ViewerController
 *
 * Produces a video-js viewer.
 * Note the poster and source references only apply to the default or expected derivative types:
 * level1=video/mp4
 * level2=montage
 */
class ViewerController {

    def gridFSService

    def index() {

        // What are we ?
        if (!params.pid) return http404()

        final file = gridFSService.findByPidAsOrfile(params.pid)
        if (file) {

            if (!file.level1) return http404()

            // width and height
            def format = file.level1.metadata.content?.format
            if (!format)     // set some default
                file.level1.metadata.content = [
                        format: [
                                height: 262,
                                width: 640
                        ]
                ]

            params.poster = (params.poster) ?: grailsApplication.config.grails.serverURL + '/file/level2/' + params.pid
            params.source = (params.source) ?: grailsApplication.config.grails.serverURL + '/file/level1/' + params.pid

            def pid = [source: params.source, poster: params.poster]
            if (file.master.metadata.pidType && file.master.metadata.pidType == 'or') {
                pid.source = file.master.metadata.resolverBaseUrl + params.pid + '?locatt=level1'
                pid.poster = file.master.metadata.resolverBaseUrl + params.pid + '?locatt=level2'
            }

            [file: file, pid:pid]

        } else {
            http404()
        }
    }

    def http404() {
        render(view: '../file/404.gsp', status: 404)
    }
}
