package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.FileSystemFactory
import org.apache.ftpserver.ftplet.FileSystemView
import org.apache.ftpserver.ftplet.FtpException
import org.apache.ftpserver.ftplet.User
import org.objectrepository.orfiles.GridFSService

public class VFSFactory implements FileSystemFactory {

    private GridFSService gridFSService

    public FileSystemView createFileSystemView(User user) throws FtpException {
        new VFSView(gridFSService, user)
    }

    public void setGridFSService(GridFSService gridFSService) {
        this.gridFSService = gridFSService
    }
}
