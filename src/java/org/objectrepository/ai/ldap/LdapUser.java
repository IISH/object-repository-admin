package org.objectrepository.ai.ldap;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;

public class LdapUser extends InetOrgPerson {

    private String homeDirectory;
    private Long gidNumber; // Posix group id
    private Long uidNumber; // Posix user id
    private String loginshell;


    protected void populateContext(DirContextAdapter adapter) {

        super.populateContext(adapter);
        adapter.setAttributeValue("homeDirectory", homeDirectory);
        adapter.setAttributeValue("gidNumber", String.valueOf(gidNumber));
        adapter.setAttributeValue("uidNumber", String.valueOf(uidNumber));
        adapter.setAttributeValue("loginshell", loginshell);

        // Will overrule super setting:
        adapter.setAttributeValue("uid", getUsername());
        adapter.setAttributeValues("objectclass", new String[]{"posixAccount", "shadowAccount", "person", "inetOrgPerson"});
    }

    public static class Essence extends InetOrgPerson.Essence {

        public Essence() {
        }

        public Essence(LdapUser copyMe) {
            super(copyMe);
            setHomeDirectory(copyMe.getHomeDirectory());
            setGidNumber(copyMe.getGidNumber());
            setUidNumber(copyMe.getUidNumber());
            setLoginshell(copyMe.getLoginshell());
            setUid(copyMe.getUsername());
            setCn(new String[]{copyMe.getUsername()});
        }

        public Essence(DirContextOperations ctx) {
            super(ctx);
            setHomeDirectory(ctx.getStringAttribute("homeDirectory"));
            setGidNumber(Long.valueOf(asLong(ctx.getStringAttribute("gidNumber"))));
            setUidNumber(Long.valueOf(asLong(ctx.getStringAttribute("uidNumber"))));
            setLoginshell(ctx.getStringAttribute("loginshell"));
            setUsername(ctx.getStringAttribute("uid"));
            setMail(ctx.getStringAttribute("mail"));
        }

        private String asLong(String gidNumber) {
            return (gidNumber == null) ? "-1" : gidNumber;
        }

        protected LdapUserDetailsImpl createTarget() {
            return new LdapUser();
        }

        public void setHomeDirectory(String homeDirectory) {
            ((LdapUser) instance).homeDirectory = homeDirectory;
        }

        public void setGidNumber(Long gidNumber) {
            ((LdapUser) instance).gidNumber = gidNumber;
        }

        public void setUidNumber(Long uidNumber) {
            ((LdapUser) instance).uidNumber = uidNumber;
        }

        public void setLoginshell(String loginshell) {
            ((LdapUser) instance).loginshell = loginshell;
        }
    }

    private String getHomeDirectory() {
        return homeDirectory;
    }

    private Long getGidNumber() {
        return gidNumber;
    }

    private String getLoginshell() {
        return loginshell;
    }

    private Long getUidNumber() {
        return uidNumber;
    }

    public String getId() {
        return getUsername();
    }
}