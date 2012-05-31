package org.objectrepository.util

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Environment
import groovy.io.FileType
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.objectrepository.instruction.Instruction

@TestMixin(GrailsUnitTestMixin)
class OrUtilTest {

    def config

    void setUp() {
        GroovyClassLoader classLoader = new GroovyClassLoader(this.class.classLoader)
                ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
                config = ConfigurationHolder.config = slurper.parse(classLoader.loadClass("WorkflowConfig"))
    }

    void testAvailablePlans() {
        final plans = OrUtil.availablePlans(config.workflow)
        assert plans.size() >  1
    }

    void testFSInstruction() {

        int success, failure
        new File(System.properties['user.dir'] + "/test/resources").eachFile(FileType.FILES) {
            if (OrUtil.hasFSInstruction(it)) {success++}
            else { failure++ }
        }
        assert success == 2
        assert failure == 3
    }

    void testFSInstructionValues() {
        def shouldHave = [
                action: "add", access: "open", contentType: "image/jpg", na: "12345",
                resolverBaseUrl: "http://hdl.handle.net/", autoGeneratePIDs: "lid",
                label: "I was put in this folder."]
        File file = new File(System.properties['user.dir'] + "/test/resources/valid-instruction.xml")
        def instruction = OrUtil.hasFSInstruction(file)
        instruction.each {
            assert it.key in shouldHave
            assert it.value == shouldHave."$it.key"
        }

        ['fileSet', 'na'].each {      key ->
            assert !instruction.find {
                it.key == key
            }
        }
    }

    void testCamelCase() {

        assert "AaaaBCccCccDdddddddddd" == OrUtil.camelCase(['Aaaa', 'B', 'cccCcc', 'ddddddddddd'])
    }

    void testTakeFirst() {
        assert 'a' == OrUtil.takeFirst(['a', 'b'])
        assert null == OrUtil.takeFirst([])
        assert null == OrUtil.takeFirst(null)
    }

    void testRemoveFirst() {
        def list = ['a', 'b']
        assert OrUtil.removeFirst(list)
        assert list == ['b']
        assert OrUtil.removeFirst(list)
        assert list.size() == 0
        assert !OrUtil.removeFirst(list)
    }


    void testPutAll()
    {
        File file = new File(System.properties['user.dir'] + "/test/resources/instruction-with-plan.xml")
        def instructionFromFile = OrUtil.hasFSInstruction(file)
        Instruction document = [:]
        OrUtil.putAll( config.workflow, document, instructionFromFile)
        assert document.plan.size() == 2
        // todo: test this:
        // assert document.workflow.get(0).limit == Integer.MAX_VALUE
    }
}
