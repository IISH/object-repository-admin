package org.objectrepository.orfiles

import org.objectrepository.instruction.Instruction
import org.objectrepository.instruction.Profile
import org.objectrepository.instruction.Stagingfile
import org.objectrepository.security.InterceptorValidation
import org.objectrepository.security.Policy
import org.objectrepository.util.OrUtil
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class OrfileController extends InterceptorValidation {

    def springSecurityService
    def workflowActiveService
    def gridFSService
    def downloadService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        forward(action: "list", params: params)
    }

    def list() {

        if (params.pid) return findbypid()

        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.offset = params.offset ? params.int('offset') : 0
        params.order = !params.order || params.order == "asc" ? 1 : -1
        params.sort = params.sort ?: "_id"
        final labels = gridFSService.labels(params.na).plus(0, 'select label')
        if (params.label == null) return [orfileInstanceListTotal: 0, labels: labels]
        def orfileInstanceList = gridFSService.findAllByLabel(params.na, params)
        [orfileInstanceList: orfileInstanceList, orfileInstanceListTotal: gridFSService
                .countByNa(params.na, params),
         labels            : labels]
    }

    def findbypid() {
        def orfileInstanceList = []
        if (params.pid) {
            def file = gridFSService.get(params.na, params.pid)
            if (file)
                orfileInstanceList << file
            else {
                def split = params.pid.split('/', 2)
                gridFSService.listFilesByObjid(params.na, 'master', (split.size() == 2) ? split[1] : split[0]).each {
                    orfileInstanceList << [master: it, level3: it]
                }
            }
        }
        final labels = gridFSService.labels(params.na).plus(0, 'select label')
        [orfileInstanceList: orfileInstanceList, orfileInstanceListTotal: orfileInstanceList.size(), labels: labels]
    }

    /**
     * show
     *
     * The PID value is composed of the na, a forward slash and a id value.
     * This is not how we want to pass on values. But the encoded forward slash ( %2F ) does give issues.
     *
     * @return
     */
    def show(String pid) {

        switch (request.format) {
            case 'html':
            case 'format':
                def orfileInstance = gridFSService.get(params.na, pid)
                if (!orfileInstance) {
                    flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), params.na + '/' + params.pid])
                    forward(action: 'list')
                } else [orfileInstance: orfileInstance]
                break
            case 'xml':
                response.setCharacterEncoding('utf-8')
                response.setContentType('text/xml')
                downloadService.writeOrfiles(params, response.outputStream)
                break
        }
    }

    def edit(String pid) {
        def orfileInstance = gridFSService.get(params.na, pid)
        if (orfileInstance)
            [orfileInstance: orfileInstance, policyList: Policy.findAllByNa(params.na), profile: Profile.findByNa(params.na)]
        else
            c(HttpStatus.NOT_FOUND)
    }

    def update(String pid) {
        def orfileInstance = gridFSService.get(params.na, pid)
        if (orfileInstance) {
            orfileInstance.master.metadata.access = params.access
            orfileInstance.master.metadata.embargo = params.embargo
            orfileInstance.master.metadata.embargoAccess = params.embargoAccess
            orfileInstance.master.metadata.label = params.label
            if (params.objid) orfileInstance.master.metadata.objid = params.objid
            if (params.seq) orfileInstance.master.metadata.seq = Integer.parseInt(params.seq)

            gridFSService.update(orfileInstance.master)

            flash.message = message(code: 'default.updated.message', args: [message(code: 'files.label', default: 'Files'), pid])
            forward(action: 'show', pid: params.pid)
        } else {
            c(HttpStatus.NOT_FOUND)
        }
    }

    /*  def update(Orfile orfile) {
          update(orfile.pid)
      }*/

    /**
     * recreate
     *
     * An instruction will be re created based on the stored files's shared label value.
     *
     * @return
     */
    def recreate() {

        def file = gridFSService.findByField(params.na, 'master', 'metadata.label', params.label)
        if (!file?.metaData?.fileSet) {
            flash.message = 'No such label: ' + params.label
            return forward(action: 'list')
        }

        def fileSet = file.metaData.fileSet
        Instruction instructionInstance = Instruction.findByFileSet(fileSet)
        if (instructionInstance) {
            if (Stagingfile.countByFileSet(file.metaData.fileSet) != 0) {
                flash.message = "There is already an instruction for this dataset: " + instructionInstance.label + " on " + fileSet
                return redirect(action: "list")
            }
            instructionInstance.workflow.clear()
        } else
            instructionInstance = new Instruction()

        sendRecreate(instructionInstance, fileSet, null)
    }

    /**
     * instruction
     *
     * This individual file will be reinserted into an instruction
     *
     * @return
     */
    def recreatefile(String pid) {
        def orfileInstance = gridFSService.findByPid(pid)
        if (!orfileInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'files.label', default: 'Files'), pid])
            return forward(action: 'list')
        }

        def stagingFile = Stagingfile.findByPid(pid)
        if (stagingFile) {
            flash.message = "This file is already staged in another instruction: " + stagingFile.parent.label
            return forward(action: 'show')
        }

        def fileSet = orfileInstance.metaData.fileSet
        def instructionInstance = Instruction.findByFileSet(fileSet)
        if (!instructionInstance) instructionInstance = new Instruction()
        sendRecreate(instructionInstance, fileSet, orfileInstance)
    }

    def sendRecreate(Instruction instruction, String fileSet, def orfileInstance) {
        instruction.na = params.na
        instruction.fileSet = fileSet
        instruction.autoIngestValidInstruction = false
        instruction.label = params.label
        instruction.task = [name: OrUtil.camelCase(['Instruction', actionName])]
        instruction.task.taskKey()
        instruction.resubmitPid = orfileInstance?.metaData?.pid

        workflowActiveService.first(instruction)
        if (instruction.save(flush: true)) {
            try {
                sendMessage("activemq:status", instruction.task.identifier)
            }
            catch (Exception e) {
                // message queue may be down
                log.warn e.message
                flash.message = e.message
            }
        }
        params.id = instruction.id
        forward(controller: 'instruction', action: 'show', id: instruction.id)
    }

}
