package org.objectrepository.ldap;

import org.springframework.ldap.core.*;
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.ldap.NameNotFoundException
import javax.naming.NameNotFoundException;

public class CustomLdapUserDetailsManager extends LdapUserDetailsManager {

    LdapTemplate _template

    public CustomLdapUserDetailsManager(ContextSource contextSource) {
        super(contextSource);
        _template = new LdapTemplate(contextSource)
    }

    def findLdapGroupUsers(String groupID) {
        DistinguishedName dn = new DistinguishedName(SpringSecurityUtils.securityConfig.ldap.authorities.groupSearchBase)
        dn.add(SpringSecurityUtils.securityConfig.ldap.groupAttribute, groupID)
        try {
            _template.lookup(dn,
                    new ContextMapper() {
                        public Object mapFromContext(Object ctx) {
                            DirContextAdapter adapter = (DirContextAdapter) ctx;
                            return adapter.getStringAttributes("uniqueMember");
                        }
                    });
        } catch (NameNotFoundException e) {
            log.error(e)
            log.error("Ask the objectrepository administrator to set the ldap group role " + dn.toUrl())
        }
    }
}