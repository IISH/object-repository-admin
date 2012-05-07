package org.objectrepository.instruction

/**
 * DownloadTestCase
 *
 * Overall integration test class. Used to build a test scaffold and is
 * responsible for the commonly used methods.
 */
class DownloadTestCase {

    protected Instruction openInstruction;
    protected Stagingfile openStagingfile, closedStagingfile;
    protected def mockMap

    protected void setUp() {
        setUpInstructions()
        setUpStagingfiles()
        def principal = [username: "cp1000", na: "12345", authorities: [authority: 'ROLE_CPUSER']]
        mockMap = [  // Some mocklike mapping
                isLoggedIn: {-> true },
                principal: principal,
                getPrincipal: {-> principal},
                hasRole: {String s -> true}
        ]
    }

    protected void tearDown() {
        openInstruction.delete(flush: true)
        openStagingfile.delete(flush: true)
        closedStagingfile.delete(flush: true)
    }

    private void setUpInstructions() {
        def sep = File.separator
        def tt = new Task(
                info: "One or more files",
                name: "failure",
                statusCode: 800
        )

        openInstruction = new Instruction(
                fileSet: System.properties['base.dir'] + sep + "test" + sep + "resources",
                label: "Test Label",
                resolverBaseUrl: "http://hdl.handle.net/",
                na: "12345",
                contentType: "image/jpg",
                access: "open",
                action: "add",
                task: tt
        )

        // Note: flushing is mandatory in integration test cases,
        // or the mongo cursor sometimes used will not be able to fetch them.
        openInstruction.save(flush: true)
    }

    private void setUpStagingfiles() {
        def sep = File.separator
        def tt = new Task(
                name: 'some task',
                info: "Empty file",
                statusCode: 800
        )
        openStagingfile = new Stagingfile(
                task: tt,
                fileSet: openInstruction.fileSet,
                location: System.properties['base.dir'] + sep +
                        "test" + sep + "resources" + sep + "test-collection"
                        + sep + "empty.jpg",
                md5: "d41d8cd98f00b204e9800998ecf8427e", // empty string
                pid: "test1",
                lid: "test1",
                contentType: 'image/jpg',
                access: "open",
                action: "add"
        )
        openStagingfile.save(flush: true)

        def tt2 = new Task(
                name: 'some task',
                info: "No file found at location",
                statusCode: 800
        )
        closedStagingfile = new Stagingfile(
                task: tt2,
                fileSet: openInstruction.fileSet,
                location: System.properties['base.dir'] + sep +
                        "test" + sep + "resources" + sep + "test-collection"
                        + sep + "notfound",
                md5: "d41d8cd98f00b204e9800998ecf8427e", // empty string
                pid: "test2",
                lid: "test2",
                contentType: "image/jpg",
                access: "open",
                action: "add"
        )
        closedStagingfile.save(flush: true)
    }

    /**
     * Validates a property map and its values against an attribute/tag map and
     * its values. All properties in the property map should be in the
     * attribute map and vice versa.
     * @param propMap The property map.
     * @param attrMap The attribute/tag map.
     */
    protected void validatePropertyMap(Map propMap, Map attrMap) {
        assertNotNull propMap
        assertNotNull attrMap
        println("propmap:")
        println(propMap)
        println("attrMap:")
        println(attrMap)
        assertEquals propMap.size(), attrMap.size()
        propMap.each { key, value ->
            if (value == null) {
                assertTrue !attrMap.containsKey(key) || attrMap[key] == null
                return
            }
            assertEquals value as String, attrMap[key]
        }
    }


}
