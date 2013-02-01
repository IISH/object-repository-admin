package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.UserManager
import org.apache.ftpserver.usermanager.UserManagerFactory

class MetsUserManagerFactory implements UserManagerFactory {

    private def adminUserDetailsService
    private def encryptor

    MetsUserManagerFactory(def adminUserDetailsService, def encryptor) {
        this.adminUserDetailsService = adminUserDetailsService
        this.encryptor = encryptor
    }

    UserManager createUserManager() {
        new MetsFtpUserManager(adminUserDetailsService, encryptor)
    }
}
