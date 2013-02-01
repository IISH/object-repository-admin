package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.FileSystemFactory
import org.apache.ftpserver.ftplet.FileSystemView
import org.apache.ftpserver.ftplet.FtpException
import org.apache.ftpserver.ftplet.User
import org.objectrepository.mets.MetsService

public class MetsFileSystemFactory implements FileSystemFactory {

    private MetsService metsService

    public FileSystemView createFileSystemView(User user) throws FtpException {
        return new MetsFileSystemView(metsService, user)
    }

    public void setMetsService(MetsService metsService) {
        this.metsService = metsService
    }
}
