package org.objectrepository.security

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService
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
    @Override
    UserDetails loadUserByUsername(String username, boolean loadRoles) {

        log.info "Attempted user logon '$username' with loadRole from database being $loadRoles"
        User.withTransaction { status ->
            def user = User.findByUsername(username)

            if (!user) {
                throw new UsernameNotFoundException('User not found', username)
            }

            if (!(user.na)) {
                throw new UsernameNotFoundException('User has not got the proper authority', username)
            }

            def roles = [new GrantedAuthorityImpl("USER_NA_" + user.na)]
            if (loadRoles) {
                log.info user.authorities
                user.authorities?.each {
                    roles << new GrantedAuthorityImpl(it.role.authority)
                }
            }
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