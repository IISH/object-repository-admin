package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.Authentication
import org.apache.ftpserver.ftplet.AuthenticationFailedException
import org.apache.ftpserver.ftplet.Authority
import org.apache.ftpserver.ftplet.User
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication
import org.apache.ftpserver.usermanager.impl.AbstractUserManager
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission
import org.apache.ftpserver.usermanager.impl.TransferRatePermission
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

import java.util.regex.Pattern

class VFSUserManager extends AbstractUserManager {

    def authenticationManager
    static int maxLogin = 0
    static int maxLoginPerIP = 0
    static int downloadRate = 0
    static int uploadRate = 0
    static int maxIdleTimeSec = 300

    VFSUserManager(def authenticationManager) {
        super("admin", null)
        this.authenticationManager = authenticationManager
    }

    User getUserByName(String username) {
        null // Method not implemented
    }

    String[] getAllUserNames() {
        [] // Method not implemented
    }

    void delete(String s) {
        // Method not implemented
    }

    void save(User user) {
        // Method not implemented
    }

    boolean doesExist(String s) {
        true // Method not implemented
    }

    /**
     * authenticate
     *
     * Authenticated the used with a passwordDecoder.
     * The encodes is set at instantiation of the FtpService
     *
     * @param authentication
     * @return
     * @throws AuthenticationFailedException
     */
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {

        if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication

            if (!(upauth.password && upauth.username))
                throw new AuthenticationFailedException("Authentication failed")

            def principal = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(upauth.username, upauth.password))?.principal
            if (principal) {
                List<Authority> authorities = new ArrayList<Authority>();
                authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP))
                authorities.add(new TransferRatePermission(downloadRate, uploadRate));

                def p = Pattern.compile('^ROLE_OR_USER_(\\d*)$|^ROLE_OR_DISSEMINATION_(\\d*)$')
                def homeDir = principal.authorities*.authority.findAll {
                    p.matcher(it).matches()
                }?.collect {
                    '/' + it.split('_').last()
                }?.join(',')
                if (!homeDir)
                    throw new AuthenticationFailedException("Authentication failed")

                p = Pattern.compile('^ROLE_OR_POLICY_(.*)$')
                def policies = ('ROLE_OR_USER' in principal.authorities*.authority) ? ['administrator'] : principal.authorities*.authority.findAll {
                    p.matcher(it).matches()
                }?.collect {
                    it.split('_').last()
                }

                def resources = null
                if (!policies && principal.authorities*.authority.find {
                    it.startsWith('ROLE_OR_DISSEMINATION_')
                }) {
                    resources = org.objectrepository.security.User.findByUsername(principal.username)?.resources
                }

                new VFSUser(name: principal.username, password: principal.password, homeDir: homeDir, authorities: authorities, policies: policies, resources: resources, maxIdleTimeSec: maxIdleTimeSec)
            } else
                throw new AuthenticationFailedException("Authentication failed")
        } else
            throw new IllegalArgumentException(
                    "Authentication not supported by this user manager")
    }

    String getAdminName() {
        adminName
    }

    boolean isAdmin(String s) {
        s == adminName
    }
}
