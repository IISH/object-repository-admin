package org.objectrepository.viewer

import org.springframework.security.access.annotation.Secured

/**
 * ViewerController
 *
 * Produces a video-js viewer.
 * Note the poster and source references only apply to the default or expected derivative types:
 * level1=video/mp4
 * level2=montage
 */
@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class ViewerController {

    def gridFSService

    def index() {

        if (!params.pid) return http404()

        final file = gridFSService.findByPidAsOrfile(params.pid)
        if (file) {

            if (!file.level1) return http404()

            // width and height
            int width = 0, height = 0
            def streams = file.level1.metadata.content?.streams
            if (streams) {
                def codec_video = streams.find {
                    it.codec_type == 'video'
                }
                if (codec_video) {
                    width = codec_video.width
                    height = codec_video.height
                }
            }

            if (width == 0 || width >= 600) {
                width = 600
                height = 450
            }

            file.level1.metadata.content = [
                    streams: [
                            width: width,
                            height: height
                    ]
            ]

            params.poster = (params.poster) ?: grailsApplication.config.grails.serverURL + '/file/level2/' + params.pid
            params.source = (params.source) ?: grailsApplication.config.grails.serverURL + '/file/level1/' + params.pid

            def pid = [source: params.source, poster: params.poster]
            if (file.master.metadata.pidType && file.master.metadata.pidType == 'or') {
                pid.source = file.master.metadata.resolverBaseUrl + params.pid + '?locatt=view:level1'
                pid.poster = file.master.metadata.resolverBaseUrl + params.pid + '?locatt=view:level2'
            }

            [file: file, pid: pid]

        } else {
            http404()
        }
    }

    def http404() {
        render(view: '../file/404.gsp', status: 404)
    }
}
