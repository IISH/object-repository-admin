package org.objectrepository.security

import grails.test.mixin.TestFor
import org.springframework.security.core.authority.GrantedAuthorityImpl

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(UserPermissionController)
class UserPermissionControllerTests {

    UserPermissionControllerTests() {
        controller.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_USER_00000')]]]
        LinkedHashMap.metaClass.encodePassword { password ->
            "encrypted password"
        }
        User.metaClass.'static'.findByUsername << { username ->
            null
        }
    }

    void testUsername() {

        params.na = 00000
        params.user = [
                mail: 'lerfref@hotmail.com',
                password: 'wedwed'
        ]
        params.'user[0].expirationDate' = '2012-10-13'
        params.'user[0].downloadLimit' = '100'
        params.'user[0].pid' = '10622/30051001064648'
        params.'user[1].expirationDate' = '2012-10-13'
        params.'user[1].downloadLimit' = '100'
        params.'user[1].pid' = '10622/30051000924891'

        request.format = 'json'
        controller.save()
        assert response.contentType == 'text/javascript'
        assert response.status == 400
        assert response.text.contains('Expecting: username')
    }
}
