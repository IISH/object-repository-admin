package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.UserManager
import org.apache.ftpserver.usermanager.UserManagerFactory

class FtpUserManagerFactory implements UserManagerFactory {

    private def providers
    private def encryptor

    FtpUserManagerFactory(def providers, def encryptor) {
        this.providers = providers
        this.encryptor = encryptor
    }

    UserManager createUserManager() {
        new VFSUserManager(providers, encryptor)
    }
}
