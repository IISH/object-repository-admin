package org.objectrepository.ftp

import org.apache.ftpserver.ftplet.Authority
import org.apache.ftpserver.ftplet.AuthorizationRequest
import org.apache.ftpserver.ftplet.User

class VFSUser implements User {

    private String name = null
    private String password = null
    private int maxIdleTimeSec = 0 // no limit
    private String homeDir = null
    private boolean isEnabled = true
    private List<Authority> authorities = new ArrayList<Authority>()
    private List<String> policies

    /**
     * Get the user name.
     */
    public String getName() {
        return name
    }

    /**
     * Get the user password.
     */
    public String getPassword() {
        password
    }

    public List<Authority> getAuthorities() {
        authorities
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = (authorities) ? Collections.unmodifiableList(authorities) : null
    }

    public void setPolicies(def policies) {
         this.policies = (policies) ?: []
    }

    /**
     * Get the maximum idle time in second.
     */
    public int getMaxIdleTime() {
        maxIdleTimeSec
    }

    /**
     * Set the maximum idle time in second.
     */
    public void setMaxIdleTime(int idleSec) {
        maxIdleTimeSec = (idleSec < 0) ? 0 : idleSec
    }

    /**
     * Get the user enable status.
     */
    public boolean getEnabled() {
        isEnabled
    }

    /**
     * Get the user home directory.
     */
    public String getHomeDirectory() {
        homeDir
    }

    /**
     * String representation.
     */
    public String toString() {
        return name
    }

    /**
     * {@inheritDoc}
     */
    public AuthorizationRequest authorize(AuthorizationRequest request) {
        // check for no authorities at all
        if (authorities == null) {
            return null
        }

        boolean someoneCouldAuthorize = false;
        for (Authority authority : authorities) {
            if (authority.canAuthorize(request)) {
                someoneCouldAuthorize = true

                request = authority.authorize(request);

                // authorization failed, return null
                if (request == null) {
                    return null
                }
            }

        }

        if (someoneCouldAuthorize) {
            request
        } else {
            null
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Authority> getAuthorities(Class<? extends Authority> clazz) {
        List<Authority> selected = new ArrayList<Authority>()

        for (Authority authority : authorities) {
            if (authority.getClass().equals(clazz)) {
                selected.add(authority)
            }
        }

        selected
    }

}
