package org.objectrepository.security

import grails.test.mixin.TestFor
import org.springframework.security.core.authority.GrantedAuthorityImpl

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(UserPermissionController)
class UserPermissionControllerTests {

    final String na = "00000"

    final User databaseUser = new User()

    void setUp() {
        LinkedHashMap.metaClass.encodePassword { password, hash ->
            "encrypted password"
        }
        User.metaClass.'static'.findByUsername << { username ->
            (databaseUser.username == username) ? databaseUser : null
        }
        User.metaClass.'static'.save << {
            databaseUser
        }
        UUID.metaClass.encodeAsMD5Bytes { password ->
            "d41d8cd98f00b204e9800998ecf8427e".bytes
        }
    }

    void testNoUsername() {

        params.na = na
        params.user = [
                mail: 'wedwed@wedwed.ede',
                password: 'wedwed'
        ]

        controller.springSecurityService = [authentication: [authorities: []]]

        request.format = 'json'
        controller.save()
        assert response.status == 400
        assert response.text.contains('Expecting: username')
    }

    void testNoAuthority() {

        params.na = na
        params.user = [
                mail: 'wedwed@wedwed.ede',
                password: 'wedwed',
                username: 'a username'
        ]

        databaseUser.na = "12345"
        databaseUser.username = params.user.username
        controller.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_USER_' + na)]]]


        controller.save()
        assert response.status == 400
        assert response.text.contains('not under your control')
    }

    /*void testResouces() {
        params.na = na
        params.user = [
                mail: 'wedwed@wedwed.ede',
                password: 'wedwed' ,
                username: 'a username'
        ]
        params.'user[0].expirationDate' = '2012-10-13'
        params.'user[0].downloadLimit' = '100'
        params.'user[0].pid' = '10622/30051001064648'
        params.'user[1].expirationDate' = '2012-10-13'
        params.'user[1].downloadLimit' = '100'
        params.'user[1].pid' = 'na/30051000924891'
    }*/
}
