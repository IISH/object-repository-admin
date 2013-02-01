package org.objectrepository.ftp

import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.listener.ListenerFactory

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
        factory.setPort(2121)
        serverFactory.addListener("default", factory.createListener())

        /*SslConfigurationFactory ssl = new SslConfigurationFactory();
        ssl.setKeystoreFile(new File(grailsApplication.config.ftp.keystoreFile));
        ssl.setKeystorePassword(grailsApplication.config.ftp.keystoreFilePassword);
        factory.setSslConfiguration(ssl.createSslConfiguration());
        factory.setImplicitSsl(true);*/

        final userManagerFactory = new MetsUserManagerFactory(userDetailsService, new ContextPasswordEncryptor(springSecurityService))
        serverFactory.setUserManager(userManagerFactory.createUserManager())

        final fileSystemFactory = MetsFileSystemFactory.newInstance()
        fileSystemFactory.metsService = metsService
        serverFactory.setFileSystem(fileSystemFactory)

        server = serverFactory.createServer()
        server.start()
    }
}
