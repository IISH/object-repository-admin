package org.objectrepository.ftp;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.objectrepository.instruction.MetsService;

public class MetsFileSystemView implements FileSystemView {

    final MetsService metsService;
    final User user;

    public MetsFileSystemView(MetsService metsService, User user) {
        this.metsService = metsService;
        this.user = user;
    }

    public FtpFile getHomeDirectory() throws FtpException {
        return new MetsFtpFile("");
    }

    public FtpFile getWorkingDirectory() throws FtpException {
        return new MetsFtpFile("");
    }

    public boolean changeWorkingDirectory(String s) throws FtpException {
        return false;
    }

    public FtpFile getFile(String s) throws FtpException {

        return new MetsFtpFile("");
    }

    public boolean isRandomAccessible() throws FtpException {
        return false;
    }

    public void dispose() {

    }
}
