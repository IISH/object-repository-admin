package org.socialhistoryservices.security;

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

/**
 * MockUser
 * <p/>
 * Returns the UserDetails
 */
final class MockUser extends GrailsUser implements Serializable {

    public static long serialVersionUID = 1L;

    String na;

    public MockUser(String na, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<GrantedAuthority> authorities, Object id) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id);
        this.na = na;
    }

    public String getNa() {
        return na;
    }
}
