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
            file.writeTo(response.outputStream) // Writes the file chunk-by-chunk
            response.outputStream.flush()
            gridFSService.update(file, params.bucket)
        } else {
            render(view: '404', statuscode: 404)
        }
    }

    def metadata = {

        def file = getFile(params)
        if (file) {
            response.contentType = "text/xml"
            file
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
            return null
        }

        if (actionName == 'metadata') return fileInstance

        if (fileInstance.metadata.access == "deleted") {
            render(view: fileInstance.metadata.access)
            return
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
