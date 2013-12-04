package org.objectrepository.ftp;


import org.apache.ftpserver.ftplet.FtpFile
import org.objectrepository.orfiles.GridFSService
import org.objectrepository.security.UserResource

public class VFSFtpFile implements FtpFile {

    private String currentFolder
    private final def user
    private long length = 0
    private long created = 0
    private boolean isDirectory = true
    private final def gridFSService
    private def bucket
    private def pid
    private def objid

    VFSFtpFile(String currentFolder, def user, def gridFSService, boolean isDirectory = true, def length = 0, def created = 0) {
        this.currentFolder = currentFolder
        this.user = user
        this.gridFSService = gridFSService
        this.isDirectory = isDirectory
        this.length = length
        this.created = created
    }

    public String getAbsolutePath() {
        currentFolder.isEmpty() ? '/' : currentFolder
    }

    public String getName() {
        currentFolder.substring(currentFolder.lastIndexOf("/") + 1)
    }

    public boolean isHidden() {
        false
    }

    public boolean isDirectory() {
        isDirectory
    }

    public boolean isFile() {
        !isDirectory
    }

    /**
     * Check if the file exists. Whilst doing so, we retrieve the PID and bucket values
     * @return
     */
    public boolean doesExist() {
        final parentFolder = currentFolder[0..currentFolder.lastIndexOf("/") - 1]
        def file = gridFSService.vfs(parentFolder, user)?.f?.find {
            parentFolder + '/' + it.n == currentFolder
        }
        if (file) {
            isDirectory = false
            length = file.l
            created = file.t
            pid = file.p
            objid = file.o
            bucket = currentFolder.split('/')[2]
            true
        } else
            false
    }

    public boolean isReadable() {
        !isDirectory
    }

    public boolean isWritable() {
        false
    }

    public boolean isRemovable() {
        false
    }

    public String getOwnerName() {
        user.username
    }

    public String getGroupName() {
        user.na
    }

    public int getLinkCount() {
        (isFile()) ? 1 : 3
    }

    public long getLastModified() {
        created
    }

    public boolean setLastModified(long l) {
        false
    }

    public long getSize() {
        length
    }

    public boolean mkdir() {
        false
    }

    public boolean delete() {
        false
    }

    public boolean move(FtpFile ftpFile) {
        false
    }

    public List<FtpFile> listFiles() {
        // now return all the files under the directory. The directory is a list of comma separated pathnames. We take the base path.
        def virtualFiles = []
        if (currentFolder.isEmpty()) {
            user.homeDirectory.split(',').each {
                virtualFiles << new VFSFtpFile(it.split('/')[1], user, gridFSService)
            }
        } else {
            def vfs = gridFSService.vfs(currentFolder, user)
            sort(vfs?.d)?.each {
                virtualFiles << new VFSFtpFile(currentFolder + '/' + it.n.trim(),
                        user,
                        gridFSService,
                        true,
                        0,
                        it.t)
            }
            sort(vfs?.f)?.each {
                virtualFiles << new VFSFtpFile(currentFolder + '/' + it.n.trim(),
                        user,
                        gridFSService,
                        false,
                        it.l,
                        it.t)
            }
        }
        virtualFiles
    }

    /**
     * sort
     *
     * Enable a clean sort by appending spaces in front of the names
     *
     * @param list
     * @return
     */
    private def sort(def list) {

        if (!list) return

        int max = list.max {
            it.n.length()
        }.n.length()
        list.each {
            int l = max - it.n.length()
            if (l > 0) it.n = " ".multiply(l) + it.n
        }
        list.sort {
            it.n
        }
    }

    public OutputStream createOutputStream(long l) throws IOException {
        throw new IOException("Method createOutputStream not implemented.");
    }

    public InputStream createInputStream(long l) throws IOException {

        def resource = user.resources?.find {
            (it.pid == pid || it.pid == objid) && bucket in it.buckets
        }
        if (resource) resource.ftpDownloads++ // oddly the user.resources?.find { ... }?ftpDownloads returns from the method
        gridFSService.findByPid(pid, bucket)?.inputStream
    }

}
