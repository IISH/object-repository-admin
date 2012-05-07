package org.objectrepository.security

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.socialhistoryservices.security.MockUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

class AdminUserDetailsService implements GrailsUserDetailsService {

    /**
     * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least one role, so
     * we give a user with no granted roles this one which gets past that restriction but
     * doesn't grant anything.
     */
    static final List NO_ROLES = [new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE)]

    @Override
    UserDetails loadUserByUsername(String username, boolean loadRoles) {

        log.info "Attempted user logon: $username"
        User.withTransaction { status ->
            def user = User.findByUsername(username)

            if (!user) {
                throw new UsernameNotFoundException('User not found', username)
            }

            def roles = NO_ROLES
            if (loadRoles) {
                def authorities = user.authorities?.collect {
                    new GrantedAuthorityImpl(it.role.authority)
                }
                if (authorities) {
                    roles = authorities
                }
            }
            roles << new GrantedAuthorityImpl("USER_NA_" + user.na)

            log.info "User roles: $roles"

            return createUserDetails(user, roles)
        }
    }

    @Override
    UserDetails loadUserByUsername(String username) {
        return loadUserByUsername(username, true)
    }

    protected UserDetails createUserDetails(User user, def Collection<GrantedAuthority> authorities) {

        new MockUser(user.na, user.username, user.password, user.enabled, user.accountNonExpired, user.credentialsNonExpired, user.accountNonLocked, authorities, user.id)
    }
}