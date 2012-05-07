package org.objectrepository.security

class User {

    Long uidNumber = 2000
    String na // group id. This is the na and inetOrgPerson.ou
    String o // organizational unit. Descriptive title of organization
    String mail
    String confirmpassword
    String verification
    Long expiration = 0 // Of the verification token
    String newpassword
    String username

    // Spring minimal User fields
    String password
    boolean enabled = true
    boolean accountNonExpired = true
    boolean accountNonLocked = true
    boolean credentialsNonExpired = true

    Set<org.objectrepository.security.Role> getAuthorities() {

        UserRole.findAllByUser(this) as Set
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
        username(blank: false, unique: true, size: 3..30, matches: /^[a-zA-Z0-9_]*/)
        mail(email: true, unique: true, blank: false)
        password(password: true, blank: false, size: 6..100)
    }

    static mapping = {
        database 'security'
        password column: '`password`'
    }

    static mapWith = "mongo"
}
