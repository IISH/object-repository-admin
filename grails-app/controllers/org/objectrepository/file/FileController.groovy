package org.objectrepository.file

import com.mongodb.gridfs.GridFSFile
import org.objectrepository.domain.File
import org.objectrepository.files.Files
import org.objectrepository.security.Policy
import grails.converters.XML

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
     * Usage: /controller/bucket/format/pid
     *
     * The bucket and format are optional and use defaults depending on the bucket value:
     * Values: bucket[value=all]/format[default=xhtml]/pid
     * Values: bucket[value=master|level1|level2|level3]/format[default=file]/pid
     *
     *
     * Example
     * /file/level1/xml/12345/abcdefg = {bucket:'level1',format:'xml',pid:'12345/abcdefg'}*
     * Because the PID value can contain a forward slash, we shall not rely on the UrlMapping but parse it here
     * to find the relevant parameters.
     */
    def index() {
        forward(action: 'files', params:params)
    }

    def files = {
        def files = getFiles(params)
        if (files) {
            GridFSFile gridFSDBFile = gridFSService.getFile(files.fileInstance)
            response.contentType = gridFSDBFile.contentType
            response.contentLength = gridFSDBFile.length
            gridFSDBFile.writeTo(response.outputStream) // Writes the file chunk-by-chunk
            response.outputStream.flush()
        }
    }

    def metadata = {
        def files = getFiles(params)
        if (files) {
            response.contentType = "text/xml"
            files as XML
        }
    }

    protected def getFiles(def params) {

        String pid = params.pid
        if (!pid) {
            redirect(action: "about")
            return null
        }

        Files filesInstance = (!pid || pid == "") ? null : Files.findByPid(pid)
        if (filesInstance == null) {
            render: 404
            return null
        }

        if (actionName == 'metadata') return

        if (filesInstance.access == "deleted") {
            render(view: filesInstance.access)
            return
        }

        Policy policy = policyService.getPolicy(filesInstance)
        String access = policy.getAccessForBucket(params.bucket)
        if (access != "open") {
            if (springSecurityService.principal == "anonymousUser"
                    || !springSecurityService.hasValidNa(filesInstance.na)) {
                render(view: "denied", status: 401, model: [access: access])
                return null
            }
        }

        File fileInstance = filesInstance.files.find {
            it.bucket == params.bucket
        }
        if (fileInstance == null) {
            render(status: 404)
            return null
        }

        [filesInstance: filesInstance, fileInstance: fileInstance, Policy: policy]
    }
}
