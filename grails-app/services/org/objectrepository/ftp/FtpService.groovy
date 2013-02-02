package org.objectrepository.ftp

import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.ssl.SslConfigurationFactory
import org.springframework.beans.factory.DisposableBean

class FtpService implements DisposableBean {

    static transactional = false
    def metsService
    def grailsApplication
    def userDetailsService
    def springSecurityService
    private FtpServer server

    void start() {

        final def serverFactory = new FtpServerFactory()

        final ListenerFactory factory = new ListenerFactory()
        factory.port = Integer.parseInt(grailsApplication.config.ftp.port ?: "21")

        def keystoreFile = grailsApplication.config.ftp.ssl.keystoreFile
        if (keystoreFile) {
            String keystoreFilePassword = grailsApplication.config.ftp.ssl.keyPassword
            assert keystoreFilePassword
            SslConfigurationFactory ssl = new SslConfigurationFactory()
            ssl.keystoreFile = new File(keystoreFile)
            ssl.keystorePassword = keystoreFilePassword
            factory.sslConfiguration = ssl.createSslConfiguration()
            assert grailsApplication.config.ftp.ssl.implicitSsl
            factory.implicitSsl = grailsApplication.config.ftp.ssl.implicitSsl.toBoolean()
        }

        serverFactory.addListener("default", factory.createListener())

        final userManagerFactory = new MetsUserManagerFactory(userDetailsService, new ContextPasswordEncryptor(springSecurityService))
        serverFactory.setUserManager(userManagerFactory.createUserManager())

        final fileSystemFactory = MetsFileSystemFactory.newInstance()
        fileSystemFactory.metsService = metsService
        serverFactory.setFileSystem(fileSystemFactory)

        server = serverFactory.createServer()
        server.start()
    }

    void destroy() {
        server?.stop()
    }
}
