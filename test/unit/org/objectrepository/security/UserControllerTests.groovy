package org.objectrepository.security

import grails.test.mixin.TestFor

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(UserController)
class UserControllerTests {

    void setUp() {
        Policy.metaClass.'static'.findAllByNa << { na ->
            new Policy(na: na, access: 'open')
        }
    }

    void testCreate() {
        params.na = '12345'
        controller.create()
        assert model.userInstance instanceof User
    }

}