package org.objectrepository.ai.ldap;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.util.Assert;

import java.util.Collection;

public class UserDetailsContextMapperImpl implements UserDetailsContextMapper {

    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<GrantedAuthority> authorities) {

        LdapUser.Essence p = new LdapUser.Essence(ctx);
        p.setUsername(username);
        p.setAuthorities(authorities);
        return p.createUserDetails();
    }

    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {

        Assert.isInstanceOf(LdapUser.class, user, "UserDetails must be an LdapUser instance");
        LdapUser p = (LdapUser) user;
        p.populateContext(ctx);
    }
}