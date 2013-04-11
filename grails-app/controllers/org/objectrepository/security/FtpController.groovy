package org.objectrepository.security

import grails.converters.JSON
import org.apache.commons.lang.RandomStringUtils
import org.objectrepository.ftp.VFSView
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.access.annotation.Secured

@Secured(['ROLE_OR_USER'])
class FtpController extends NamingAuthorityInterceptor {

    static allowedMethods = [save: "POST", update: "POST"]

    def springSecurityService
    def gridFSService

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        final list = User.findAllByNa(params.na)
        [userInstanceList: list, userInstanceTotal: list.size()]
    }

    def create() {
        [userInstance: new User(params)]
    }

    def save() {

        // Set password when it was left empty
        final def newPassword
        if (params.password == "" && params.confirmpassword == "") {
            newPassword = RandomStringUtils.random(6, true, false)
            params.password = newPassword
            params.confirmpassword = newPassword
        } else
            newPassword = params.password
        params.username = params.username.toLowerCase()

        if (User.findByUsername(params.username)) {
            flash.message = "User already has an account in LDAP. Choose a different name."
            render(view: "create", model: [userInstance: params])
            return
        }

        // confirm password check
        if (!params.skippassword) {
            if (params.password != params.confirmpassword) {
                flash.message = "Passwords do not match"
                render(view: "create", model: [userInstance: params])
                return
            }
        }
        params.password = springSecurityService.encodePassword(params.password, UUID.randomUUID().encodeAsMD5Bytes())

        def userInstance = new User(params)
        if (!userInstance.save(flush: true)) {
            render(view: "create", model: [userInstance: userInstance])
            return
        }

        if (params.sendmail) {
            sendMail {
                to params.mail
                from grailsApplication.config.mail.from
                subject message(code: "mail.user.created.subject")
                body message(code: "mail.user.created.body", args: [springSecurityService.principal.username,
                        grailsApplication.config.grails.serverURL,
                        grailsApplication.config.mail.sftpServer,
                        params.id,
                        newPassword])
            }
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        forward(action: "show", id: userInstance.id)
    }

    def show(Long id) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
        }
        else
            [userInstance: userInstance]
    }

    def edit(Long id) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
        }
        else
            [userInstance: userInstance]
    }

    def update(Long id, Long version) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
            return
        }
        else if (version != null) {
            if (userInstance.version > version) {
                userInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'user.label', default: 'User')] as Object[],
                        "Another ftp has updated this User while you were editing")
                render(view: "edit", model: [userInstance: userInstance])
                return
            }
        }

        userInstance.properties = params

        if (!userInstance.save(flush: true)) {
            render(view: "edit", model: [userInstance: userInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        forward(action: "show", id: userInstance.id)
    }

    def delete(Long id) {
        def userInstance = User.findByIdAndNa(id, params.na)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
            return
        }

        try {
            userInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            log.warn(e)
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            forward(action: "show", id: id)
        }
    }

    /**
     * showHomeDirectory
     *
     * Render all selected nodes from the root
     *
     * @return
     */
    def homeDirectory() {

        response.setContentType('application/json')
        assert params.id
        final homeDir = '/' + params.na + ':' + params.id
        final _view = new VFSView(gridFSService, [homeDirectory: homeDir])
        _view.currentFolder = homeDir

        def tree = [title: homeDir, isFolder: true, key: params.na, isLazy: params.isLazy, hideCheckbox: true,
                unselectable: true, expand: true]
        tree['children'] = treeChildren(_view.workingDirectory)
        render tree as JSON
    }

    /**
     * workingDirectory
     *
     * Show the first level of children of the workingDirectory node
     *
     * @param key
     * @return
     */
    def workingDirectory(String key) {

        response.setContentType('application/json')
        def view = new VFSView(gridFSService, [homeDirectory: '/' + params.na])
        if (view.changeWorkingDirectory(key)) {
            render view.workingDirectory.listFiles().collect {
                final childKey = key + '/' + it.name
                [title: it.name, isFolder: it.directory, key: childKey, isLazy: true]
            } as JSON
        }
    }

    def updateDirectory(){
        println(params)
    }

    private def treeChildren(def f) {
        f.listFiles().collect {
            def item = [title: it.name, isFolder: it.directory, key: it.name.split(':')[0], isLazy: params.isLazy]
            if (it.directory) {
                final _view = new VFSView(gridFSService, [homeDirectory: '/' + params.na + ':' + params.id])
                _view.currentFolder += '/' + it.name
                item['children'] = treeChildren(it)
            }
            item
        }
    }
}
