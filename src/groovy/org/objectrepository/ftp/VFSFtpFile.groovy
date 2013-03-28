package org.objectrepository.ftp;


import org.apache.ftpserver.ftplet.FtpFile
import org.apache.ftpserver.ftplet.User
import org.objectrepository.orfiles.GridFSService

public class VFSFtpFile implements FtpFile {

    private String currentFolder
    private User user
    private long length
    private long created
    private boolean isDirectory
    private GridFSService gridFSService
    private def bucket
    private def pid

    public VFSFtpFile(String currentFolder, User user, def gridFSService, boolean isDirectory = true, def length = 0, def created = 0) {
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
        def file = gridFSService.vfs(parentFolder)?.f?.find {
            parentFolder + '/' + it.n == currentFolder
        }
        if (file) {
            isDirectory = false
            length = file.l
            created = file.t
            pid = file.p
            bucket = currentFolder.split('/')[2]
            return true
        }
        false
    }

    public boolean isReadable() {
        user.homeDirectory.split(',').find { // make sure we are allowed to see this
            currentFolder.startsWith(it)
        } != null
    }

    public boolean isWritable() {
        false
    }

    public boolean isRemovable() {
        false
    }

    public String getOwnerName() {
        "user"
    }

    public String getGroupName() {
        "group"
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
        }
        else {
            def vfs = gridFSService.vfs(currentFolder)
            vfs?.d?.each {
                virtualFiles << new VFSFtpFile(currentFolder + '/' + it.n, user, gridFSService)
            }
            vfs?.f?.each {
                virtualFiles << new VFSFtpFile(currentFolder + '/' + it.n, user, gridFSService, false, it.l, it.t)
            }
        }
        virtualFiles
    }

    public OutputStream createOutputStream(long l) throws IOException {
        throw new IOException("Method createOutputStream not implemented.");
    }

    public InputStream createInputStream(long l) throws IOException {
        gridFSService.findByPid(pid, bucket)?.inputStream
    }
}
