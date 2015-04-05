package org.objectrepository.instruction
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class TaskTest {

    void testTask() {
        Task task = new Task()
        assert !task.identifier
        assert task.taskKey()
        assert task.identifier
    }
}
