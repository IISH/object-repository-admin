package org.objectrepository.security

class NamingAuthorityInterceptor {

    /**
     * beforeInterceptor
     *
     * Anyone with the correct NA may see this controller's actions. Including admins ( 0 )
     */
    def beforeInterceptor = {
        if (springSecurityService.isLoggedIn() && (springSecurityService.hasNa(params.na) || springSecurityService.hasNa("0"))) {
            true
        } else {
            redirect(controller: 'login', action:'c403')
            false
        }
    }
}
