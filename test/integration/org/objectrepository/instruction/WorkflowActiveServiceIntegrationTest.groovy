package org.objectrepository.instruction

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.objectrepository.domain.File

@TestMixin(GrailsUnitTestMixin)
class WorkflowActiveServiceIntegrationTest {

    def WorkflowActiveService workflowActiveService
    def TaskValidationService taskValidationService

    // Test not complete
    void testFromQueue() {

        String fileSet = "a fileSet"
        String pid = "123213211/4f0da70a3bfa4ae383b83c66"

        WorkflowActiveService.metaClass.runMethod = { def document ->
            assert document != null
            assert document instanceof Stagingfile
            assert document.pid == pid
        }

        final String body = '<or xmlns="http://objectrepository.org/instruction/1.0/" action="add" access="open" contentType="image/jpg" na="12345"' +
                '    resolverBaseUrl="http://hdl.handle.net/" autoGeneratePIDs="lid"' +
                '    label="My alias for a folder"' +
                '    fileSet="' + fileSet + '">' +
                '    <task>' +
                '        <name>InstructionUpload</name>' +
                '        <statusCode>500</statusCode>' +
                '        <info>testCreate</info>' +
                '    </task>' +
                '    <stagingfile>' +
                '        <id>4f0da70a3bfa4ae383b83c66</id>' +
                '        <pid>' + pid + '</pid>' +
                '        <fileSet>' +
                '            C:/Users/lwo/object-repository-servicenodes/instruction-manager/src/test/resources/home/12345/folder_of_cpuser/test-collection' +
                '        </fileSet>' +
                '        <lid>lid1</lid>' +
                '        <location>' +
                '            C:/Users/lwo/object-repository-servicenodes/instruction-manager/src/test/resources/home/12345/folder_of_cpuser/test-collection/apple2/apple4/apple4someEmpty.jpg' +
                '        </location>' +
                '        <md5>41a355d12b3f8d00ea6721a4095ae9e2</md5>' +
                '    </stagingfile>' +
                '</or>'

        Stagingfile stagingfile = [pid: pid, location: '.', fileSet: fileSet, md5: '41a355d12b3f8d00ea6721a4095ae9e2']
        Task task = new Task(name: 'StagingfileIngestLevel2', statusCode: 600, date: new Date(1))
        Instruction instruction = [fileSet: fileSet, na: 'a na', file: stagingfile, access: 'the access', action: 'add', contentType: 'a contentType', task: task]
        instruction.save(flush: true)
        instruction.save()
        stagingfile.save()
        workflowActiveService.fromQueue(body)
    }

    void testHasFile() {

        String pid = "12345/abcdefg"
        def document = [pid: pid, md5: "830142f566b47d0021877a5d8979cc97"]
        File master = [
                chunkSize: 262144,
                length: 4313904,
                md5: "",
                filename: "2.tif",
                contentType: "image/jpg",
                uploadDate: "2012-01-15T18:32:36.359Z",
                aliases: null,
                metadata: {
                    pids: [
                            pid
                    ]
                },
                bucket: "master",
                timesAccessed: 0,
                timesUpdated: 2,
                firstUploadDate: "2012-01-12T16:38:09.273Z",
                lastUploadDate: "2012-01-15T18:32:37.162Z"]


        File level1 = [
                chunkSize: 262144,
                length: 4313904,
                md5: "",
                filename: "2.tif",
                contentType: "image/jpg",
                uploadDate: "2012-01-15T18:32:36.359Z",
                aliases: null,
                metadata: {
                    pids: [
                            pid
                    ]
                },
                bucket: "level1",
                timesAccessed: 0,
                timesUpdated: 2,
                firstUploadDate: "2012-01-12T16:38:09.273Z",
                lastUploadDate: "2012-01-15T18:32:37.162Z"]

        File level2 = [
                chunkSize: 262144,
                length: 4313904,
                md5: "",
                filename: "2.tif",
                contentType: "image/jpg",
                uploadDate: "2012-01-15T18:32:36.359Z",
                aliases: null,
                metadata: {
                    pids: [
                            pid
                    ]
                },
                bucket: "level2",
                timesAccessed: 0,
                timesUpdated: 2,
                firstUploadDate: "2012-01-12T16:38:09.273Z",
                lastUploadDate: "2012-01-15T18:32:37.162Z"]

        org.objectrepository.files.Files files = [
                pid: pid,
                na: "12345",
                resolverBaseUrl: "http://hdl.handle.net/",
                access: "open",
                label: "\"My alias for a folder\"",
                files: [master, level1, level2]
        ]
        files.save(flush: true)

        assert taskValidationService.hasMasterFile(document)
        assert taskValidationService.hasFile(document, "level2")
        assert !taskValidationService.hasFile(document, "i do not exist")

        document.pid = "i do not exist"
        assert !taskValidationService.hasMasterFile(document)
        assert !taskValidationService.hasFile(document, "level1")


    }
}
