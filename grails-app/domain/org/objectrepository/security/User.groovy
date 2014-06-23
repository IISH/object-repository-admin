package org.objectrepository.security

import java.util.regex.Pattern

class User {

    String na
    String mail
    String username
    String confirmpassword
    String verification
    Long expiration = 0 // Of the verification token
    String newpassword
    String url
    boolean replaceKey = true

    // Spring minimal User fields
    String password
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    List<UserResource> resources = []

    Set<Role> getAuthorities() {
        (enabled) ? UserRole.findAllByUser(this).collect { it.role } as Set : [] as Set
    }

    /**
     * authorities
     *
     * Produces a list of dissemination authorities
     *
     * @param userInstance
     * @return
     */
    List<String> authoritiesFiltered(String pattern) {

        def p = Pattern.compile(pattern)
        authorities.findAll {
            (pattern) ? p.matcher(it.authority).matches() : true
        }?.collect {
            it.authority.split('_').last()
        }
    }

    static transients = ["confirmpassword", "replaceKey", "url"]

    static constraints = {
        na(nullable: true, blank: false)
        verification(nullable: true, unique: true)
        newpassword(nullable: true)
        username(blank: false, unique: true, size: 3..100, matches: /^[a-zA-Z0-9_\.@]*/)
        mail(email: true, blank: false)
        password(password: true, blank: false, size: 6..100)
    }

    static mapping = {
        database 'security'
        password column: '`password`'
    }

    static embedded = ['resources']

    static mapWith = "mongo"
}
