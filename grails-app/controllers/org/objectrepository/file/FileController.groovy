package org.objectrepository.file

import org.objectrepository.security.User
import org.objectrepository.util.OrUtil

import javax.servlet.http.HttpServletResponse

/**
 *     FileController
 *
 *     Implements:
 *     Access matrix
 *     Delivers the metadata of a file in a particular bucket
 *     Delivers the file ( bytestream ) in the corresponding contentType
 */
class FileController {

    def policyService
    def gridFSService
    def downloadService

    /**
     * files
     *
     * Usage: /controller/bucket/pid
     *
     * Returns the file with the known contentType
     * http 206 is supported, though not the lists of ranges (multipart)
     *
     * The client may set the contentType to other formats.
     * In case of 'application/save' the response will suggest a filename.
     *
     * Example
     * /file/level1/12345/abcdefg = {bucket:'level1',pid:'12345/abcdefg'}*
     * Because the PID value can contain a forward slash, we made it's capture greedy in the UrlMappings.
     */
    def file = {
        boolean isHead = (request.method.toLowerCase() == 'head')
        final def file = getFile(params, isHead)
        if (file) {
            webRequest.renderView = false
            response.contentType = (params.contentType) ?: file.contentType
            response.setHeader('Last-Modified', String.format('%ta, %<td %<tb %<tY %<tT GMT', file.uploadDate))

            Date begin = new Date()
            long from = 0
            final String range = request.getHeader('Range')
            if (range) {
                log.info("range : " + range)
                def m = range.substring('bytes='.length()) =~ /(\d*)-(\d*)/
                if (m[0][1] == "") { // -[n]
                    from = file.length - m[0][2] as long
                    to = file.length - 1
                } else if (m[0][2] == "") { // [n]-
                    from = m[0][1] as long
                    to = file.length - 1
                } else { //[n]-[m]
                    from = m[0][1] as long
                    to = m[0][2] as long
                }

                long contentLength = 1 + to - from
                response.setHeader('Content-Length', contentLength.toString() )
                response.setHeader('Content-Range', 'bytes ' + from + '-' + to + '/' + file.length)

                if (isHead) {
                    webRequest.renderView = false
                    response.status = HttpServletResponse.SC_OK
                    return null
                }

                if (range.contains(',')) {   // we do not support a multipart response
                    render HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE
                    return null
                }

                response.status = HttpServletResponse.SC_PARTIAL_CONTENT
                int r = gridFSService.range(response.outputStream, file, from, to)
                if (r != -1) response.status = r

            } else {
                response.setHeader('Content-Length', file.length.toString() )
                response.status = HttpServletResponse.SC_OK

                if (params.contentType == 'application/save') {
                    def filename = (params.filename) ?: file.filename
                    response.setHeader 'Content-disposition', 'attachment; filename="' + filename + '"'
                }

                if (!isHead)
                    file.writeTo(response.outputStream)
            }

            Date end = new Date()

            if (grailsApplication.config.siteusage && from == 0L && System.getProperty("layout", "not") == 'disseminate')
                stats(file, begin, new Date().time - begin.time)

            log.info String.format("Done writing file in %s seconds", (end.time - begin.time) / 1000)
        }
    }

    private void stats(def file, Date begin, long downloadTime) {
        log.info "Increment statistics"
        final String ip = request.getHeader('X-Forwarded-For') ?: request.getRemoteAddr()
        def document = [pid: file.metaData.pid,
                bucket: params.bucket,
                c: '??',
                ip: ip,
                downloadDate: begin,
                downloadTime: downloadTime]
        request.getHeaderNames().each {
            document.put(it, request.getHeader(it))
        }
        gridFSService.siteusage(file.metaData.na, document)
    }

    def metadata = {

        if (params.pid) {
            params.na = OrUtil.getNa(params.pid)

            def accepts = (params.accept) ? [params.accept] : request.getHeaders('accept')*.toLowerCase()
            if (accepts.any {
                it.contains('htm')
            }) {
                def orfileInstance = gridFSService.findByPidAsOrfile(params.pid)
                if (orfileInstance)
                    [orfileInstance: orfileInstance, params: params]
                else
                    render(view: '404', statuscode: 404)
            } else {
                response.setCharacterEncoding("utf-8");
                response.setContentType("text/xml")
                downloadService.writeOrfiles(params, response.outputStream)
            }
        } else
            redirect(action: "about")
    }

    def deleted = {
        [params: params]
    }

/**
 * getFile
 *
 * Retrieved the GridFSFile.
 *
 * @param params
 * @return
 */
    protected def getFile(def params, boolean isHead) {

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

        final def access = policyService.hasAccess(fileInstance, params.bucket, params.cache)
        if (access.status == 200) {
            if (!isHead && access.level != 'open')
                access.user?.save(flush: false)
            return fileInstance
        }

        render(view: "denied", status: access.status, model: [level: access.level])
    }
}
