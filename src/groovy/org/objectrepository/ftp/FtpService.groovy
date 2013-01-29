package org.objectrepository.ftp

import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.Authority
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor
import org.apache.ftpserver.usermanager.UserFactory
import org.apache.ftpserver.usermanager.impl.WritePermission

class FtpService {

    static transactional = false

    public FtpService(def metsService) {

        final def serverFactory = new FtpServerFactory();

        System.out.println("Adding Users Now");
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(new File("C:/temp/users.properties"));

        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
        def userManagement = userManagerFactory.createUserManager();
        serverFactory.setUserManager(userManagement);
        UserFactory userFact = new UserFactory();
        userFact.setName("test");
        userFact.setPassword("test");
        userFact.setHomeDirectory("C:/temp/test");
        List<Authority> authorityList = new ArrayList<Authority>(1);
        authorityList.add(new WritePermission());
        userFact.setAuthorities(authorityList);
        def user = userFact.createUser();
        userManagement.save(user);

        //final fileSystemFactory = MetsFileSystemFactory.newInstance()
        //fileSystemFactory.metsService = metsService
        //serverFactory.setFileSystem(fileSystemFactory);

        def server = serverFactory.createServer();
        server.start();
    }
}
