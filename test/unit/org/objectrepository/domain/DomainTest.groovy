package org.objectrepository.domain

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.objectrepository.instruction.Task

@TestMixin(GrailsUnitTestMixin)
class DomainTest {

    void testTaskKey(){

        Task task = new Task()
        assert !task.identifier
        assert task.taskKey()
        assert task.identifier
    }

}
