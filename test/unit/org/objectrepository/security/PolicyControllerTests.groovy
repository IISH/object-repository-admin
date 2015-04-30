package org.objectrepository.security

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(PolicyController)
@TestMixin(GrailsUnitTestMixin)
class PolicyControllerTests {

    void setUp() {
        ArrayList.metaClass.hasNa { na ->
            '12345' == na
        }
        controller.springSecurityService = []

        Policy.metaClass.'static'.save << {
            delegate
        }
    }

    void testCreate() {
        controller.create()
        assert model.policyInstance instanceof Policy
    }

    void testSave() {

        def cmd = mockCommandObject(Policy)
        cmd.na = params.na = '12345'
        cmd.access = 'custom'
        cmd.validate()
        controller.save(cmd)
        assert view == '/policy/show.gsp'

        cmd.na = params.na = '67890'
        controller.save(cmd)
        assert view == '/layouts/403'
    }

}
