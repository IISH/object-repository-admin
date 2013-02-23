package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.Authentication
import org.apache.ftpserver.ftplet.Authority
import org.apache.ftpserver.ftplet.User
import org.apache.ftpserver.usermanager.impl.AbstractUserManager
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission
import org.apache.ftpserver.usermanager.impl.TransferRatePermission
import org.apache.ftpserver.ftplet.AuthenticationFailedException
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication
import org.apache.ftpserver.usermanager.AnonymousAuthentication

class VFSUserManager extends AbstractUserManager {

    def adminUserDetailsService
    static int maxLogin = 0
    static int maxLoginPerIP = 0
    static int downloadRate = 0
    static int uploadRate = 0
    static int maxIdleTimeSec = 300

    VFSUserManager(def adminUserDetailsService, def encryptor) {
        super("admin", encryptor)
        this.adminUserDetailsService = adminUserDetailsService
    }

    User getUserByName(String username) {
        def details = adminUserDetailsService.loadUserByUsername(username)
        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP))
        authorities.add(new TransferRatePermission(downloadRate, uploadRate));
        new VFSUser(name: details.username, password: details.password, homeDir: '/'+details.na, authorities: authorities, maxIdleTimeSec: maxIdleTimeSec)
    }

    String[] getAllUserNames() {
        // Method not implemented
        null
    }

    void delete(String s) {
        // Method not implemented
    }

    void save(User user) {
        // Method not implemented
    }

    boolean doesExist(String s) {
        adminUserDetailsService.loadUserByUsername(s)
    }

    public User authenticate(Authentication authentication) throws AuthenticationFailedException {

        if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication

            String user = upauth.getUsername()
            String password = upauth.getPassword()

            if (user == null || password == null) {
                throw new AuthenticationFailedException("Authentication failed")
            }

            User userCandidate = getUserByName(user)
            if ( getPasswordEncryptor().matches(userCandidate?.password, getPasswordEncryptor().encrypt(password))) {
                return userCandidate
            } else {
                throw new AuthenticationFailedException("Authentication failed")
            }

        } else if (authentication instanceof AnonymousAuthentication) {
            if (doesExist("anonymous")) {
                return getUserByName("anonymous")
            } else {
                throw new AuthenticationFailedException("Authentication failed")
            }
        } else {
            throw new IllegalArgumentException(
                    "Authentication not supported by this user manager")
        }
    }

    String getAdminName() {
        adminName
    }

    boolean isAdmin(String s) {
        s == adminName
    }
}
