package org.objectrepository.util

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.io.FileType
import org.objectrepository.instruction.Instruction
import org.objectrepository.instruction.Task

@TestMixin(GrailsUnitTestMixin)
class OrUtilTest {

    def workFlowJob

    void setUp() {
    }

    void testFSInstruction() {

        int success, failure
        new File(System.properties['user.dir'] + "/test/resources").eachFile(FileType.FILES) { // Todo: How to groovy the resource folder ?
            if (OrUtil.hasFSInstruction(it)) {success++}
            else { failure++ }
        }
        assert success == 1
        assert failure == 3
    }

    void testHasTask() {

        Instruction instruction = new Instruction()
        instruction.workflow = [
                new Task(name: 'a'),
                new Task(name: 'i am a task')
        ]
        assert OrUtil.hasTask(instruction.workflow, new Task(name: 'i am a task'))
        assert !OrUtil.hasTask(instruction.workflow, new Task(name: 'i am not in the list'))
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
}
