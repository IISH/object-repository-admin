package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.UserManager
import org.apache.ftpserver.usermanager.UserManagerFactory

class FtpUserManagerFactory implements UserManagerFactory {

    private def adminUserDetailsService
    private def encryptor

    FtpUserManagerFactory(def adminUserDetailsService, def encryptor) {
        this.adminUserDetailsService = adminUserDetailsService
        this.encryptor = encryptor
    }

    UserManager createUserManager() {
        new VFSUserManager(adminUserDetailsService, encryptor)
    }
}
