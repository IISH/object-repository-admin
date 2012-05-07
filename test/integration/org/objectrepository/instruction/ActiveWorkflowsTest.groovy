package org.objectrepository.instruction

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class ActiveWorkflowsTest {

    def workflowActiveService

    void setUp() {

        Instruction.collection.remove()
        Stagingfile.collection.remove()
        Task.collection.remove()
    }


    void testSaveWorkflowMethod() {

        def sep = "/"

        Task task = [name: 'Start', statusCode: 0]

        final String fileSet = System.properties['base.dir'] + sep + "test" + sep + "resources"
        Instruction document = new Instruction(
                fileSet: fileSet,
                label: "Test Label",
                resolverBaseUrl: "http://hdl.handle.net/",
                na: "12345",
                contentType: "image/jpg",
                access: "open",
                action: "add",
                task: task
        )

        document.save(flush: true)

        // Will pass through all the phases with some little settings here
        workflowActiveService.runMethod(document)
        assert document.task.name == 'FileBindPIDs'
        assert document.task.statusCode == 300 // Sent to queue

        // Is the document stored ?
        Instruction documentPersisted = Instruction.get(document.id)
        assert documentPersisted.task.name == 'FileBindPIDs'
        assert documentPersisted.task.statusCode == 300
    }


}
