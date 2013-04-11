package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.FileSystemView
import org.apache.ftpserver.ftplet.FtpException
import org.apache.ftpserver.ftplet.FtpFile
import org.objectrepository.orfiles.GridFSService

public class VFSView implements FileSystemView {

    final GridFSService gridFSService
    final def user
    String currentFolder

    public VFSView(GridFSService gridFSService, def user) {
        this.gridFSService = gridFSService
        this.user = user
        this.currentFolder = ""
    }

    public FtpFile getHomeDirectory() throws FtpException {
        new VFSFtpFile("", user, gridFSService)
    }

    public FtpFile getWorkingDirectory() throws FtpException {
        new VFSFtpFile(currentFolder, user, gridFSService)
    }

    /*
    changeWorkingDirectory

    Sets the new working directory.
     */

    int i = 0

    public boolean changeWorkingDirectory(String s) throws FtpException {

        if (s == "" || s == '/') {
            currentFolder = ""
            return true
        }
        if (s == './') return true // PWD
        if (s == '..') return changeWorkingDirectory(CDUP(currentFolder)) //CDUP
        if (s[0] != '/') s = currentFolder + '/' + s // cd to subfolder in same folder

        if (user.homeDirectory.split(',').find { // make sure we are allowed to see this
            (s + '/').startsWith(it.split(':')[0] + '/')
        }) {
            if (!gridFSService.vfs(s)) return false
            currentFolder = s
            true
        } else false
    }

    public FtpFile getFile(String s) throws FtpException {
        if (s == "./") {
            s = currentFolder
        }
        else if (s[0] != '/') { // directory or file in same folder
            s = currentFolder + '/' + s
        }
        new VFSFtpFile(s, user, gridFSService)
    }

    public boolean isRandomAccessible() throws FtpException {
        return false;
    }

    void dispose() {
    }

    /**
     * CDUP
     *
     * Returns the parent or self if there is no parent path
     * / = /
     * /folder = /
     * /folder a/folder b = /folder a
     * /folder a/folder b/folder c = /folder a/folder b
     *
     * @param s
     * @return the parent
     */
    private String CDUP(String s) {
        def t = s.tokenize("/")
        if (t.size() < 2) return ""
        t.remove(t.size() - 1)
        "/" + t.join("/")
    }
}