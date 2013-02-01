package org.objectrepository.ftp

import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.ssl.SslConfigurationFactory

class MetsFtpService {

    static transactional = false
    def metsService
    def grailsApplication
    def userDetailsService
    def springSecurityService
    private FtpServer server

    void start() {

        final def serverFactory = new FtpServerFactory()

        final ListenerFactory factory = new ListenerFactory()
        factory.port = (grailsApplication.config.ftp.port) ?: 2121
        serverFactory.addListener("default", factory.createListener())

        def keystoreFile = grailsApplication.config.ftp?.keystoreFile
        if (keystoreFile) {
            String keystoreFilePassword = grailsApplication.config.ftp.keystoreFilePassword
            SslConfigurationFactory ssl = new SslConfigurationFactory()
            ssl.keystoreFile = new File(keystoreFile)
            ssl.keyPassword = keystoreFilePassword
            ssl.keystoreAlgorithm = "RSA"
            factory.sslConfiguration  = ssl.createSslConfiguration()
            factory.implicitSsl = true
        }

        final userManagerFactory = new MetsUserManagerFactory(userDetailsService, new ContextPasswordEncryptor(springSecurityService))
        serverFactory.setUserManager(userManagerFactory.createUserManager())

        final fileSystemFactory = MetsFileSystemFactory.newInstance()
        fileSystemFactory.metsService = metsService
        serverFactory.setFileSystem(fileSystemFactory)

        server = serverFactory.createServer()
        server.start()
    }
}
