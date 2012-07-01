package org.objectrepository.file

import org.objectrepository.security.Policy

/**
 *     FileController
 *
 *     Implements:
 *     Access matrix
 *     Delivers the metadata of a file in a particular bucket
 *     Delivers the file ( bytestream ) in the corresponding contentType
 */
class FileController {

    def springSecurityService
    def policyService
    def gridFSService

    /**
     * index
     *
     * Usage: /controller/bucket/pid
     *
     * Example
     * /file/level1/12345/abcdefg = {bucket:'level1',pid:'12345/abcdefg'}*
     * Because the PID value can contain a forward slash, we made it greedy in the UrlMappings.
     */
    def file = {
        def file = getFile(params)
        if (file) {
            response.contentType = file.contentType
            response.contentLength = file.length
            log.info "Writing file to browser"
            Date begin = new Date()
            file.writeTo(response.outputStream) // Writes the file chunk-by-chunk
            log.info "Flushing"
            response.outputStream.flush()
            int downloadTime = new Date().time - begin.time
            if (System.getProperty("layout", "not") == 'disseminate') {
                log.info "Increment statistics"
                final String ip = request.getHeader('X-Forwarded-For') ?: request.getRemoteAddr()
                def document = [pid: file.metaData.pid,
                        bucket: params.bucket,
                        referer: request.getHeader('referer'),
                        ip: ip,
                        downloadDate: begin,
                        downloadTime: downloadTime]
                gridFSService.siteusage(file.metaData.na, document)
            }
            log.info "Done"
        }
    }

    def metadata = {

        String pid = params.pid
        if (!pid) {
            redirect(action: "about")
            return null
        }

        def orfileInstance = gridFSService.findByPidAsOrfile(pid)
        if (orfileInstance) {
            [orfileInstance: orfileInstance]
        } else {
            render(view: '404', statuscode: 404)
        }
    }

    protected def getFile(def params) {

        String pid = params.pid
        if (!pid) {
            redirect(action: "about")
            return null
        }

        def fileInstance = gridFSService.findByPid(pid, params.bucket)
        if (fileInstance == null) {
            render(view: '404', statuscode: 404)
            return null
        }

        if (fileInstance.metadata.access == "deleted") {
            render(view: '404', statuscode: 404)
            return null
        }

        Policy policy = policyService.getPolicy(fileInstance)
        String access = policy.getAccessForBucket(params.bucket)
        if (access != "open") {
            if (springSecurityService.principal == "anonymousUser"
                    || !springSecurityService.hasValidNa(fileInstance.metadata.na)) {
                render(view: "denied", status: 401, model: [access: access])
                return null
            }
        }

        fileInstance
    }
}
