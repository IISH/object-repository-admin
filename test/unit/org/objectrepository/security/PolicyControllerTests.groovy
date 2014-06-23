package org.objectrepository.security

import grails.converters.XML
import grails.test.mixin.TestFor

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(PolicyController)
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
        cmd.na = '12345'
        cmd.access = 'custom'
        cmd.validate()
        controller.save(cmd)
        assert view == '/policy/show.gsp'

        cmd.na = '67890'
        controller.save(cmd)
        assert view == 'create'
    }

}
