package org.objectrepository.instruction

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Environment
import org.bson.types.ObjectId
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.objectrepository.files.GridFSService
import org.objectrepository.util.OrUtil

@TestMixin(GrailsUnitTestMixin)
class WorkflowActiveServiceTest {

    def WorkflowActiveService workflowActiveService
    def TaskValidationService taskValidationService
    def fileSet_Instruction
    def fileSet_NoInstruction
    def config

    void setUp() {
        GroovyClassLoader classLoader = new GroovyClassLoader(this.class.classLoader)
        ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
        config = ConfigurationHolder.config = slurper.parse(classLoader.loadClass("WorkflowConfig"))

        WorkflowJob.metaClass.message = { def document ->  // make sure we skip the message queue method and move right to the last document
            document.task.info += "," + document.task.name
            last(document)
        }

        WorkflowJob.metaClass.saveWorkflow = { def document ->  // make sure we skip the message queue method and move right to the last document
            null
        }

        Stagingfile.metaClass.getActionDefault = {
            getProperty("action")
        }

        Stagingfile.metaClass.delete = {
            println("Mock delete")
            true
        }

        workflowActiveService = new WorkflowActiveService()
        workflowActiveService.taskValidationService = taskValidationService = new TaskValidationService()
        workflowActiveService.grailsApplication = taskValidationService.grailsApplication = ConfigurationHolder

        // This fileSet has a instruction.xml in it
        fileSet_NoInstruction = System.properties['base.dir'] + "/test/resources/home/12345/folder_of_cpuser/test-templates"
        fileSet_Instruction = System.properties['base.dir'] + "/test/resources/home/12345/folder_of_cpuser/test-collection"

        workflowActiveService.gridFSService = new GridFSService()
    }

    void testFirst() {
        Task task = [name: 'TestTaskLevelA', statusCode: 800]
        Instruction document = [fileSet: 'dummy', na: '00000', contentType: 'image/jpeg', task: task]
        workflowActiveService.first(document)
        assert document.task.name == 'TestTaskLevelA'
        assert document.task.statusCode == 0
    }

    void testLast() {
        Task task = [name: 'TestTaskLevelA', statusCode: 0]
        Instruction document = [fileSet: 'dummy', na: '00000', contentType: 'image/jpeg', task: task]
        workflowActiveService.last(document)
        assert document.task.name == 'TestTaskLevelA'
        assert document.task.statusCode == 800
    }

    void testNext() {
        Task task = [name: 'TestTaskLevelB', statusCode: 100]
        Instruction document = [fileSet: 'dummy', na: '00000', contentType: 'image/jpeg', task: task]
        workflowActiveService.next(document)
        assert document.task.name == 'TestTaskLevelB'
        assert document.task.statusCode == 200
    }

    void testChangeWorkflow() {
        Task task = [name: 'TestTaskLevelA', statusCode: 500]
        Instruction document = [fileSet: 'dummy', na: '00000', contentType: 'image/jpeg', task: task]
        workflowActiveService.changeWorkflow('TestTaskLevelC', document)
        assert document.task.name == 'TestTaskLevelC'
        assert document.task.statusCode == 100

        workflowActiveService.changeWorkflow('TestTaskLevelB200', document)
        assert document.task.name == 'TestTaskLevelB'
        assert document.task.statusCode == 200
    }

    void testTask500() {
        Task task = [name: 'TestTaskLevelC', statusCode: 500]
        Instruction document = [fileSet: 'dummy', na: '00000', contentType: 'image/jpeg', task: task]
        workflowActiveService.task500(document)
        assert document.task.name == 'TestTaskLevelC'
        assert document.task.statusCode == 600
    }

    void testTask800End() {
        Task task = [name: 'TestTaskLevelB', statusCode: 500]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', task: task, action: 'add']
        document.parent = [id: new ObjectId()]
        document.workflow = []
        workflowActiveService.Stagingfile800(document)
        assert document.task.name == 'EndOfTheRoad'
        assert document.task.statusCode == 100
    }

    void testTask800EndFromRunMethod() {
        Task task = [name: 'TestTaskLevelB', statusCode: 500]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', task: task, action: 'add']
        document.parent = [id: new ObjectId()]
        document.failed = []
        document.workflow = []
        workflowActiveService.runMethod(document)
        assert document.task.name == 'EndOfTheRoad'
        assert document.task.statusCode == 800
    }

    void testTask800Next() {
        Task task1 = [name: 'TestTaskLevelB', statusCode: 500]
        Task task2 = [name: 'TestTaskLevelC', statusCode: 500]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', task: task1, action: 'add']
        document.parent = [id: new ObjectId()]
        document.workflow = [task1, task2]
        workflowActiveService.Stagingfile800(document)
        assert document.task.name == 'TestTaskLevelC'
        assert document.task.statusCode == 100
    }

    void testTask800NextFromRunMethod() {
        Task task1 = [name: 'TestTaskLevelB', statusCode: 500]
        Task task2 = [name: 'TestTaskLevelC', statusCode: 500]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', task: task1, action: 'add']
        document.parent = [id: new ObjectId()]
        document.workflow = [task1, task2]
        workflowActiveService.runMethod(document)
        assert document.task.name == 'TestTaskLevelC'
        assert document.task.statusCode == 900
    }

    /**
     * Will pass through all the phases. We skip the message queue by our dynamic overload of the message method.
     * New staged files always receive their tasks from the instruction.
     */
    void testSelectedAllFlows() {

        Task task = [name: 'Start', statusCode: 100]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', task: task, action: 'add']
        document.parent = [id: new ObjectId()]
        document.parent.workflow = OrUtil.availableWorkflows(config.workflow)
        document.failed = []
        workflowActiveService.runMethod(document)
        assert document.task.name == 'EndOfTheRoad'
        assert document.task.statusCode == 800
        def taskList = document.task.info.split(",")
        document.parent.workflow.each {
            assert it.name in taskList
        }
    }

    /**
     * See if we can select our tasks
     */
    void testSelectedFlows() {

        Task task = [name: 'Start', statusCode: 100]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', task: task, action: 'upsert']
        def allWorkflow = OrUtil.availableWorkflows(config.workflow)
        document.parent = [id: new ObjectId()]
        document.parent.workflow = [
                allWorkflow[1],
                allWorkflow[2]
        ] as List<Task>
        document.failed = []
        workflowActiveService.runMethod(document)
        assert document.task.name == 'EndOfTheRoad'
        assert document.task.statusCode == 800
        def taskList = document.task.info.split(",")
        document.parent.workflow.each {
            assert it.name in taskList
        }
        assert !(allWorkflow[0].name in taskList)
        assert !(allWorkflow[3].name in taskList)
    }

    /**
     * Check to see if new instructions and filesets are detected
     */
    void testUploadFiles() {
        Task task = [name: 'UploadFiles', statusCode: 0]
        Instruction document = [fileSet: '.', na: '00000', contentType: 'image/jpeg', task: task]
        workflowActiveService.UploadFiles(document)
        assert document.task.name == 'UploadFiles'
        assert document.task.statusCode == 800

        document.task = [name: 'TestTaskLevelA', statusCode: 0]
        document.fileSet = 'i.do.not.exist'
        def method = null
        try {
            workflowActiveService.UploadFiles(document)
        } catch (Exception e) {
            method = e.method
        }
        assert method == null
    }

    void testUploadFiles800() {
        Task task = [name: 'TestTaskLevelA', statusCode: 800]
        Instruction document = [fileSet: '.', na: '00000', contentType: 'image/jpeg', task: task]
        workflowActiveService.UploadFiles800(document)
        assert document.task.name == 'TestTaskLevelA'// As the folder '.' exist, but has no instruction; it ought to remain where it is
        assert document.task.statusCode == 800

        document.fileSet = fileSet_Instruction // This has an instruction, so there should be an advancement
        workflowActiveService.UploadFiles800(document)
        assert document.task.name == 'InstructionUpload'
        assert document.task.statusCode == 100

        document.fileSet = 'i.am.a.lie'
        document.task.name = 'UploadFiles'
        document.task.statusCode = 800
        workflowActiveService.UploadFiles800(document)
        assert document.task.name == 'UploadFiles'
        assert document.task.statusCode == 0
    }

    void testFileFlowsDelete() {

        Task task = [name: 'Start', statusCode: 100]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', contentType: 'image/jpeg', task: task, action: 'delete']
        document.parent = [id: new ObjectId(), workflow: OrUtil.availableWorkflows(config.workflow)]
        document.failed = []
        workflowActiveService.runMethod(document)
        assert document.task.name == 'EndOfTheRoad'
        assert document.task.statusCode == 800
    }

    void testChange() {
        def document = [change: false, task: [name: 'Start', statusCode: 0]]
        def currentTask = workflowActiveService.currentTask(document)
        assert !workflowActiveService.taskValidationService.hasChange(currentTask, document)
        document.task.name = 'Boo!'
        assert workflowActiveService.taskValidationService.hasChange(currentTask, document)
    }

    void testTaskLimit3() {

        Task task = [name: 'TestTaskLevelB', statusCode: 500]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', task: task, action: 'add']
        workflowActiveService.changeWorkflow('TestTaskLevelC', document)
        assert document.task.limit == 3
    }

    void testTaskInfinite() {

        Task task = [name: 'TestTaskLevelB', statusCode: 500]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', task: task, action: 'add']
        workflowActiveService.changeWorkflow('StagingfileIngestMaster', document)
        assert document.task.limit == Integer.MAX_VALUE
    }
}