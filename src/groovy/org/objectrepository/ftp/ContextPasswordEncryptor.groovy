package org.objectrepository.ftp

import org.apache.ftpserver.usermanager.PasswordEncryptor

/**
 * ContextPasswordEncryptor
 *
 * We use the same password encryptor as the Spring setting
 *
 */
class ContextPasswordEncryptor implements PasswordEncryptor {

    private def springSecurityService

    ContextPasswordEncryptor(def springSecurityService) {
        this.springSecurityService = springSecurityService
    }

    String encrypt(String s) {
        springSecurityService.encodePassword(s)
    }

    boolean matches(String s, String s1) {
        s == s1
    }
}
