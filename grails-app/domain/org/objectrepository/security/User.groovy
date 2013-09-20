package org.objectrepository.security

class User {

    String na
    String mail
    String confirmpassword
    String verification
    Long expiration = 0 // Of the verification token
    String newpassword
    String username

    // Spring minimal User fields
    String password
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    String useFor = 'dissemination'
    List<UserResource> resources = []

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }

    static transients = ["confirmpassword"]

    static constraints = {
        na(nullable: true, blank: false)
        useFor(inList:['administration', 'dissemination'])
        verification(nullable: true, unique: true)
        newpassword(nullable: true)
        username(blank: false, unique: true, size: 3..30, matches: /^[a-zA-Z0-9_\.]*/)
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
