package org.objectrepository.ftp;


import org.apache.ftpserver.ftplet.FtpFile
import org.apache.ftpserver.ftplet.User
import org.objectrepository.files.GridFSService

public class VFSFtpFile implements FtpFile {

    private String currentFolder
    private User user
    private long length
    private long created
    private boolean isDirectory
    private GridFSService gridFSService
    private def bucket
    private def pid

    public VFSFtpFile(String currentFolder, User user, def gridFSService, boolean isDirectory = true, long length = 0, long created = 0) {
        this.currentFolder = currentFolder
        this.user = user
        this.gridFSService = gridFSService
        this.isDirectory = isDirectory
        this.length = length
        this.created = created
    }

    public String getAbsolutePath() {
        currentFolder
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
        final parent = currentFolder[0..currentFolder.lastIndexOf("/") - 1]
        def vfs = gridFSService.vfs(parent)
        vfs?.f?.find {
            parent + '/' + it.n == currentFolder
        } != null
    }

    public boolean isReadable() {
        true
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
        if (currentFolder == "/") {
            user.homeDirectory.split(',').each {
                virtualFiles << new VFSFtpFile(it.split('/')[0], user, gridFSService)
            }
        }
        else {
            def vfs = gridFSService.vfs(currentFolder)
            vfs?.d?.each {
                virtualFiles << new VFSFtpFile(currentFolder + '/' + it.n, user, gridFSService, true)
            }
            vfs?.f?.each {
                virtualFiles << new VFSFtpFile(currentFolder + '/' + it.n, user, gridFSService, false, it.l as long , it.c as long )
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
