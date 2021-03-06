package org.objectrepository.instruction

import com.gmongo.GMongo
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Environment
import org.bson.types.ObjectId
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.objectrepository.orfiles.GridFSService
import org.objectrepository.util.OrUtil

@TestMixin(GrailsUnitTestMixin)
class WorkflowActiveServiceTest {

    def WorkflowActiveService workflowActiveService
    def WorkflowInitiateService workflowInitiateService
    def TaskValidationService taskValidationService
    def fileSet_Instruction
    def fileSet_NoInstruction
    def config

    void setUp() {
        GroovyClassLoader classLoader = new GroovyClassLoader(this.class.classLoader)
        ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
        config = slurper.parse(classLoader.loadClass("PlanConfig"))

        WorkflowJob.metaClass.message = { def document ->  // make sure we skip the message queue method and move right to the last document
            document.task.info = document.task.name
            last(document)
        }

        WorkflowJob.metaClass.saveWorkflow = { def document ->  // make sure we skip the message queue method and move right to the last document
            null
        }

        Stagingfile.metaClass.getActionDefault = {
            getProperty("action")
        }

        workflowActiveService = new WorkflowActiveService()
        workflowActiveService.metaClass.mongo = new GMongo()
        workflowActiveService.taskValidationService = taskValidationService = new TaskValidationService()
        workflowActiveService.grailsApplication = taskValidationService.grailsApplication = ConfigurationHolder

        workflowInitiateService = new WorkflowInitiateService()
        workflowInitiateService.metaClass.mongo = new GMongo()
        workflowInitiateService.taskValidationService = taskValidationService = new TaskValidationService()
        workflowInitiateService.grailsApplication = taskValidationService.grailsApplication = ConfigurationHolder

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
        Task task1 = [name: 'TestTaskLevelB', statusCode: 500]
        Task task2 = [name: 'EndOfTheRoad', statusCode: 100]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', action: 'add']
        document.parent = [id: new ObjectId()]
        document.workflow = [task1, task2]
        workflowActiveService.Stagingfile800(document)
        assert document.task.name == 'EndOfTheRoad'
        assert document.task.statusCode == 100
    }

    void testTask800EndFromRunMethod() {
        Task task1 = [name: 'TestTaskLevelB', statusCode: 500]
        Task task2 = [name: 'EndOfTheRoad', statusCode: 100]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', action: 'add']
        document.parent = [id: new ObjectId()]
        document.workflow = [task1, task2]
        workflowActiveService.runMethod(document)
        assert document.task.name == 'EndOfTheRoad'
        assert document.task.statusCode == 900
    }

    void testTask800Next() {
        Task task1 = [name: 'TestTaskLevelB', statusCode: 500]
        Task task2 = [name: 'TestTaskLevelC', statusCode: 100]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', action: 'add']
        document.parent = [id: new ObjectId()]
        document.workflow = [task1, task2]
        workflowActiveService.Stagingfile800(document)
        assert document.task.name == 'TestTaskLevelC'
        assert document.task.statusCode == 100
    }

    void testTask800NextFromRunMethod() {
        Task task1 = [name: 'TestTaskLevelB', statusCode: 500]
        Task task2 = [name: 'TestTaskLevelC', statusCode: 100]
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
                contentType: 'image/jpeg', task: task, action: 'add', objid:'hello', seq:1]
        document.parent = [id: new ObjectId()]
        document.parent.plan = OrUtil.availablePlans(config.plans)
        OrUtil.removeFirst(document.parent.plan) //  remove the InstructionPackage
        workflowActiveService.runMethod(document)
        assert document.task.name == 'EndOfTheRoad'
        assert document.task.statusCode == 900
        document.parent.plan.each { plan ->
            def t = document.workflow.find {
                it.info == plan
            }
            assert t
        }
    }

    /**
     * See if we can select our tasks
     */
    void testSelectedFlows() {

        Task task = [name: 'Start', statusCode: 100]
        Stagingfile document = [fileSet: fileSet_Instruction, na: '00000', pid: '123/12321312', md5: 'wedewdewdew',
                contentType: 'image/jpeg', task: task, action: 'upsert', objid:'hello', seq:1]
        def allWorkflow = OrUtil.availablePlans(config.plans)
        OrUtil.removeFirst(allWorkflow) //  remove the InstructionPackage
        document.parent = [id: new ObjectId()]
        document.parent.plan = [
                allWorkflow[1], // StagingfileBindPIDs
                allWorkflow[3] // StagingfileIngestLevel1
        ] as List<Task>
        workflowActiveService.runMethod(document)
        assert document.task.name == 'EndOfTheRoad'
        assert document.task.statusCode == 900
        document.parent.plan.each { plan ->
            assert document.workflow.find {
                it.info == plan
            }
            assert document.workflow.find {
                it.queue == 'StagingfileIngestLevel1Image'
            }
        }
        assert !document.workflow.find {
            it.name == allWorkflow[0] // StagingfileIngestMaster should not be in the workflow
        }
        assert !document.workflow.find {
            it.name == allWorkflow[4] // StagingfileIngestLevel2 should not be in the workflow
        }
    }

    /**
     * Check to see if new instructions and filesets are detected
     */
    void testUploadFiles() {
        Task task = [name: 'UploadFiles', statusCode: 0]
        Instruction document = [fileSet: '.', na: '00000', contentType: 'image/jpeg', task: task]
        workflowInitiateService.UploadFiles(document)
        assert document.task.name == 'UploadFiles'
        assert document.task.statusCode == 800

        document.task = [name: 'TestTaskLevelA', statusCode: 0]
        document.fileSet = 'i.do.not.exist'
        def method = null
        try {
            workflowInitiateService.UploadFiles(document)
        } catch (Exception e) {
            method = e.method
        }
        assert method == null
    }

    void testUploadFiles800() {
        Task task = [name: 'TestTaskLevelA', statusCode: 800]
        Instruction document = [fileSet: '.', na: '00000', contentType: 'image/jpeg', task: task]
        workflowInitiateService.UploadFiles800(document)
        assert document.task.name == 'TestTaskLevelA'// As the folder '.' exist, but has no instruction; it ought to remain where it is
        assert document.task.statusCode == 800

        document.fileSet = fileSet_Instruction // This has an instruction, so there should be an advancement
        workflowInitiateService.UploadFiles800(document)
        assert document.task.name == 'InstructionUpload'
        assert document.task.statusCode == 100

        document.fileSet = 'i.am.a.lie'
        document.task.name = 'UploadFiles'
        document.task.statusCode = 800
        workflowInitiateService.UploadFiles800(document)
        assert document.task.name == 'UploadFiles'
        assert document.task.statusCode == 0
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


    void testContentType() {
        Task task = [name: 'sometask', statusCode: 0]
        Stagingfile document = [contentType: 'image/jpeg']
        document.workflow = [task]
        workflowInitiateService.renameQueueWithContentType(document)

        assert document.task.queue == 'sometaskImage'
    }

    void testAlternativeContentType() {
        Task task = [name: 'sometask', statusCode: 0]
        Stagingfile document = [contentType: 'application/mxf']
        document.workflow = [task]
        workflowInitiateService.renameQueueWithContentType(document)

        assert document.task.queue == 'sometaskVideo'
    }

    void testInstructionPackage() {
        Task task = [name: 'InstructionIngest', statusCode: 100]
        Instruction document = [fileSet: 'dummy', na: '00000', contentType: 'image/jpeg', task: task, plans: ['InstructionPackage']]
        workflowActiveService.runMethod(document)
        document.task.queue == 'InstructionPackage'
        assert document.task.statusCode == 800
    }

}