package org.objectrepository.security

class User {

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

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }

    /**
     * getFirstRole
     *
     * Returns the first role from all roles
     *
     * @return
     */
    protected String getFirstRole() {
        authorities.collect {it.role.authority}.first()
    }

    /**
     * getRoles
     *
     * Returns the roles as a comma separated list
     *
     * @return
     */
    protected String getRoles() {
        authorities.collect() {it.role.authority}.join(",")
    }

    static transients = ["confirmpassword"]

    static constraints = {
        verification(nullable: true, unique: true)
        newpassword(nullable: true)
        username(blank: false, unique: true, size: 3..30, matches: /^[a-zA-Z0-9_\.]*/)
        mail(email: true, unique: true, blank: false)
        password(password: true, blank: false, size: 6..100)
    }

    static mapping = {
        database 'security'
        password column: '`password`'
    }

    static mapWith = "mongo"
}
