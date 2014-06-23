package org.objectrepository.security

import org.springframework.http.HttpStatus

class InterceptorValidation {

    /**
     * beforeInterceptor
     *
     * Anyone with the correct NA may use this controller's actions. Including admins ( 0 )
     */
    def beforeInterceptor = {
        if (springSecurityService.isLoggedIn() && (springSecurityService.hasNa(params.na) || springSecurityService.has('0'))) {
            true
        } else {
            redirect(controller: 'login', action: 'c403')
            false
        }
    }

    /**
     * status
     *
     * Determines the HTTP status of the response
     * @param o A instance of a domain class
     *
     * @return An HttpStatus
     */
    HttpStatus status(def o) {

        o?.na = params.na
        def status = _status(o)
        if (status == HttpStatus.FORBIDDEN || status == HttpStatus.NOT_FOUND)
            c(status)
        response.status = status.value()
        status
    }

    private HttpStatus _status(def o) {
        if (o == null)
            HttpStatus.NOT_FOUND
        else if (springSecurityService.hasNa(o.na)) {
            /*if (params.version) {
                def version = params.version.toLong()
                if (o.version > version) {

                    o.errors.rejectValue("version", "default.optimistic.locking.failure",
                            [message(code: 'instruction.label', default: 'Instruction')] as Object[],
                            "Another user has updated this Instruction while you were editing")
                }
            }*/
            (o.validate()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        } else
            HttpStatus.FORBIDDEN
    }


    void c(HttpStatus code) {
        switch (request.format) {
            case 'html':
            case 'form':
                render view: '/layouts/' + code.value(), model: [params: params]
                break
        }
    }
}
