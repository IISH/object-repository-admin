package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.UserManager
import org.apache.ftpserver.usermanager.UserManagerFactory
import org.springframework.security.authentication.AuthenticationManager

class FtpUserManagerFactory implements UserManagerFactory {

    private def authenticationManager

    FtpUserManagerFactory(def authenticationManager) {
        this.authenticationManager = authenticationManager
    }

    UserManager createUserManager() {
        new VFSUserManager(authenticationManager)
    }
}
