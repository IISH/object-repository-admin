package org.objectrepository.ftp

import org.apache.ftpserver.DataConnectionConfigurationFactory
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.ssl.SslConfigurationFactory
import org.springframework.beans.factory.DisposableBean

class FtpService implements DisposableBean {

    static transactional = false
    def gridFSService
    def grailsApplication
    def authenticationManager
    private FtpServer server

    void start() {

        final def serverFactory = new FtpServerFactory()

        final def factory = new ListenerFactory()
        factory.port = Integer.parseInt(grailsApplication.config.ftp.port ?: "21")

        def keystoreFile = new File(grailsApplication.config.ftp.ssl.keystoreFile ?: "dummy")
        if (keystoreFile.exists()) {
            String keystoreFilePassword = grailsApplication.config.ftp.ssl.keyPassword ?: "changeit"
            SslConfigurationFactory ssl = new SslConfigurationFactory()
            ssl.keystoreFile = keystoreFile
            ssl.keystorePassword = keystoreFilePassword
            factory.sslConfiguration = ssl.createSslConfiguration()
            assert grailsApplication.config.ftp.ssl.implicitSsl
            factory.implicitSsl = grailsApplication.config.ftp.ssl.implicitSsl.toBoolean()
        }

        final DataConnectionConfigurationFactory dataConnConfigFac = new DataConnectionConfigurationFactory()
        if (grailsApplication.config.ftp.passivePorts) dataConnConfigFac.setPassivePorts(grailsApplication.config.ftp.passivePorts)
        if (grailsApplication.config.ftp.passiveExternalAddress) dataConnConfigFac.setPassiveExternalAddress(grailsApplication.config.ftp.passiveExternalAddress)
        factory.setDataConnectionConfiguration(dataConnConfigFac.createDataConnectionConfiguration())

        serverFactory.addListener("default", factory.createListener())
        final userManagerFactory = new FtpUserManagerFactory(authenticationManager)
        serverFactory.setUserManager(userManagerFactory.createUserManager())
        final fileSystemFactory = VFSFactory.newInstance()
        fileSystemFactory.gridFSService = gridFSService
        serverFactory.setFileSystem(fileSystemFactory)
        final ftplet = new VFSFtplet()
        ftplet.gridFSService = gridFSService
        serverFactory.ftplets.put('VFSFtplet', ftplet)
        server = serverFactory.createServer()
        server.start()
    }

    void destroy() {
        server?.stop()
    }
}
