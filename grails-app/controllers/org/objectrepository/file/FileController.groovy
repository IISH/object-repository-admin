package org.objectrepository.file

import org.objectrepository.util.OrUtil

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
            response.contentType = (params.contentType) ?: file.contentType
            response.contentLength = file.length
            if ( params.contentType == 'application/octet-stream') response.setHeader 'Content-disposition", "attachment; filename="${file.filename}"'
            log.info "Writing file to client"
            Date begin = new Date()
            file.writeTo(response.outputStream) // Writes the file chunk-by-chunk
            int downloadTime = new Date().time - begin.time
            if (System.getProperty("layout", "not") == 'disseminate') {
                log.info "Increment statistics"
                final String ip = request.getHeader('X-Forwarded-For') ?: request.getRemoteAddr()
                def document = [pid: file.metaData.pid,
                        bucket: params.bucket,
                        ip: ip,
                        downloadDate: begin,
                        downloadTime: downloadTime]
                request.getHeaderNames().each {
                    document.put(it, request.getHeader(it))
                }
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

        def accept = params.accept
        switch (accept) {
            case 'application/xml':
            case 'text/xml':
                response.setCharacterEncoding("utf-8");
                response.setContentType("text/xml")
                gridFSService.writeOrfiles(params, OrUtil.getNa(params.pid), response.outputStream)
                break;
            case 'text/javascript':
            case 'text/json':
            default:
                def orfileInstanceList = gridFSService.findByPidAsOrfile(pid)
                if (orfileInstanceList) {
                    [orfileInstanceList: orfileInstanceList]
                }
                else {
                    render(view: '404', statuscode: 404)
                }
        }
    }

    def deleted = {
        [params:params]
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

        final String access = policyService.getPolicy(fileInstance).getAccessForBucket(params.bucket)
        if (access != "open" && !springSecurityService.hasValidNa(fileInstance.metadata.na)) {
            render(view: "denied", status: 401, model: [access: access])
        }
        else
            fileInstance
    }
}
