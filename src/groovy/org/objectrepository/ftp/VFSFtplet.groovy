package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.DefaultFtplet
import org.apache.ftpserver.ftplet.FtpException
import org.apache.ftpserver.ftplet.FtpRequest
import org.apache.ftpserver.ftplet.FtpSession
import org.apache.ftpserver.ftplet.FtpletResult
import org.objectrepository.orfiles.GridFSService

class VFSFtplet extends DefaultFtplet {

    private GridFSService gridFSService

    @Override
    FtpletResult onDownloadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {

        session.user.resources?.each {
            if (it.ftpDownloads > 0)
                gridFSService.updateResource(session.user.name, it.pid, it.ftpDownloads)
        }
        super.onDownloadEnd(session, request)
    }

    public void setGridFSService(GridFSService gridFSService) {
        this.gridFSService = gridFSService
    }
}
