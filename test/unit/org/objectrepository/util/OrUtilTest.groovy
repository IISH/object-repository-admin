package org.objectrepository.util
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Environment
import groovy.io.FileType
import org.objectrepository.instruction.Instruction

@TestMixin(GrailsUnitTestMixin)
class OrUtilTest {

    def config

    void setUp() {
        GroovyClassLoader classLoader = new GroovyClassLoader(this.class.classLoader)
        ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
        config = slurper.parse(classLoader.loadClass("PlanConfig"))
    }

    void testAvailablePlans() {
        final plans = OrUtil.availablePlans(config.plans)
        assert plans.size() > 1
    }

    void testFSInstruction() {

        int success = 0, failure = 0
        new File(System.properties['user.dir'] + "/test/resources").eachFile(FileType.FILES) {
            if (OrUtil.hasFSInstruction(it)) {success++}
            else { failure++ }
        }
        assert success == 3
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

    void testPutAll() {
        def document = new Instruction()
        File file = new File(System.properties['user.dir'] + "/test/resources/instruction-with-plan.xml")
        def instructionFromFile = OrUtil.hasFSInstruction(file)
        OrUtil.putAll(config.plans, instructionFromFile, document)
        assert instructionFromFile.plan.size() == 2
        assert instructionFromFile.action == 'upsert'
        assert document.action == 'upsert'
    }

    void testPutAllNoPlan() {
        def document = new Instruction()
        File file = new File(System.properties['user.dir'] + "/test/resources/instruction-without-plan.xml")
        def instructionFromFile = OrUtil.hasFSInstruction(file)
        OrUtil.putAll(config.plans, instructionFromFile, document)
        assert instructionFromFile.plan.size() == 0
        assert instructionFromFile.action == 'upsert'
        assert document.action == 'upsert'
    }

    /**
     * testGetOr
     *
     * In all these situations ought to be "12345" or "1012345
     *
     * 12345/mypid
     * hdl:12345/mypid
     * ark:/12345/654xz321
     * 10.12345/jmbi.1998.2354
     * doi:10.12345/jmbi.1998.2354
     */
    void testGetOr() {
        def nas = ['12345/mypid', 'hdl:12345/mypid', 'ark:/12345/mypid', '10.12345/mypid.678.9', 'doi:10.12345/mypid.678.9']
        nas.each {
            final String na = OrUtil.getNa(it)
            assert na == "12345" || na == "1012345"
        }
    }
}
