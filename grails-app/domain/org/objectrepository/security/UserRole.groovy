package org.objectrepository.security

import org.apache.commons.lang.builder.HashCodeBuilder

class UserRole implements Serializable {

    User user
    Role role

    boolean equals(other) {
        if (!(other instanceof UserRole)) {
            return false
        }

        other.user?.id == user?.id &&
                other.role?.id == role?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (user) builder.append(user.id)
        if (role) builder.append(role.id)
        builder.toHashCode()
    }

    static UserRole get(long userId, long roleId) {
        find 'from UserRole where user.id=:userId and role.id=:roleId',
                [userId: userId, roleId: roleId]
    }

    static UserRole create(User user, Role role, boolean flush = false) {
        new UserRole(user: user, role: role).save(flush: flush, insert: true)
    }

    static boolean remove(User user, Role role, boolean flush = false) {
        UserRole instance = UserRole.findByUserAndRole(user, role)
        instance ? instance.delete(flush: flush) : false
    }

    static void removeAll(User user) {
        UserRole.findAllByUser(user).each {
            it.delete()
        }
    }

    static void removeAll(Role role) {
        UserRole.findAllByUser(role).each {
            it.delete()
        }
    }

    static mapping = {
        database 'security'
        composite: ['role', 'user']
        //id composite: ['role', 'user'], name:"id"
    }
    static mapWith = "mongo"
}