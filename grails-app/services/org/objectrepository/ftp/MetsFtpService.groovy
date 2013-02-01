package org.objectrepository.ftp

import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory

class MetsFtpService {

    static transactional = false
    def metsService
    def grailsApplication
    def userDetailsService
    def springSecurityService
    private FtpServer server

    void start() {

        final def serverFactory = new FtpServerFactory()

        final userManagerFactory = new MetsUserManagerFactory(userDetailsService, new ContextPasswordEncryptor(springSecurityService))
        serverFactory.setUserManager(userManagerFactory.createUserManager())

        final fileSystemFactory = MetsFileSystemFactory.newInstance()
        fileSystemFactory.metsService = metsService
        serverFactory.setFileSystem(fileSystemFactory)

        server = serverFactory.createServer()
        server.start()
    }
}
