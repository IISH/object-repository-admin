package org.objectrepository.orfiles

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.objectrepository.security.Policy
import org.objectrepository.security.User
import org.objectrepository.security.UserController

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(UserController)
@TestMixin(GrailsUnitTestMixin)
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