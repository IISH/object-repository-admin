package org.objectrepository.instruction

import grails.converters.XML
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.util.slurpersupport.NodeChild
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.objectrepository.util.OrUtil

/**
 * Integration test suite for the InstructionController.
 */
@TestMixin(GrailsUnitTestMixin)
public class InstructionControllerTest extends DownloadTestCase {

    public InstructionControllerTest() {
        InstructionController.metaClass.getActionName() {
            "download"
        }
    }

    /**
     * Checks if an Instruction instance is equivalent to a xml representation
     * given.
     * @param ot The Instruction instance.
     * @param root The XML representation.
     */
    private void validateInstruction(Instruction ot, NodeChild root) {

        assertNotNull ot
        assertNotNull root

        // Validate the persistent properties
        def propMap = OrUtil.getPropertiesMap(ot, true, [])
        propMap.remove("task")
        def attrMap = root.attributes()
        attrMap.remove("xmlns")
        validatePropertyMap(propMap, attrMap)

        if (ot.task) {
            // Instead of root['task'], use this in order to be able to check
            // for null values.
            def exists = false
            root.children().each { exists |= it.name() == "task"}
            assertTrue exists
            validateTask(ot.task, root.task)
        }
    }

    /**
     * Validate a single Task object against its XML representation.
     * @param tasktype The task type object.
     * @param taskNode The XML representation of the task type.
     */
    private void validateTask(Task tasktype, def taskNode) {
        assertNotNull tasktype
        assertNotNull taskNode

        def propMap = OrUtil.getPropertiesMap(tasktype, true, [])
        def tagMap = nodeChildrenToMap(taskNode)
        validatePropertyMap(propMap, tagMap)
    }

    /**
     * Validate a collection of Orfile Types, matching the keys of the passed
     * map with the pid attribute of the children of the  passed root node.
     * @param Stagingfiles The map of file type objects.
     * @param root The root node (or) which has files as children.
     */
    private void validateStagingfiles(Map<String, Stagingfile> Stagingfiles,
                                   NodeChild root) {
        assertNotNull root?.file
        // Do not care about the size for now. It could possibly be more files are returned because of multiple people running tests on
        // one database.
        root.file.each {
            if (Stagingfiles.containsKey(it.pid.text())) {
                validateStagingfile(Stagingfiles[it.pid.text()], it)
            }
        }
    }

    /**
     * Validate a single Stagingfile object against its XML representation.
     * @param Stagingfile The file type object.
     * @param fileNode The XML representation.
     */
    private void validateStagingfile(Stagingfile stagingfile, def fileNode) {
        assertNotNull Stagingfile
        assertNotNull fileNode

        def propMap = OrUtil.getPropertiesMap(Stagingfile, true, [])
        propMap.remove("task")
        def tagMap = nodeChildrenToMap(fileNode)
        tagMap.remove("task")
        validatePropertyMap(propMap, tagMap)

        if (stagingfile.task) {
            def exists = false
            fileNode.children().each { exists |= it.name() == "task"}
            assertTrue exists
            validateTask(stagingfile.task, fileNode.task)
        }
    }

    /**
     * Helper to convert children tags of a node to a key-value map.
     * @param node The node to map the children of.
     * @return A key-value map of the node's children.
     */
    private Map nodeChildrenToMap(def node) {
        def map = [:]
        node.children().each {
           if ( !OrUtil._filter.contains(it.name())) map.put(it.name(), it.text())
        }
        return map
    }

    /**
     * Test valid call to the download controller.
     */
    public void testDownload() {

        def c = new InstructionController()
        c.springSecurityService = mockMap
        c.params.id = openInstruction.id
        c.download();
        assertEquals "utf-8", c.response.getCharacterEncoding()
        assertEquals "text/xml", c.response.getContentType()

        // Parse the XML response.
        def root = null
        try {
            root = XML.parse c.response.contentAsString
            assertTrue root instanceof NodeChild
        } catch (ConverterException e) {
            fail(e.getMessage())
        }

        // Validate instruction
        validateInstruction(openInstruction, root)

        // Validate Stagingfiles.
        Map<String, Stagingfile> m = [test1: openStagingfile, test2: closedStagingfile]
        validateStagingfiles(m, root)
    }

    /**
     * Test invalid calls to the download controller.
     */
    public void testInvalidDownload() {

        def c = new InstructionController()
        c.springSecurityService = mockMap

        // No/invalid id param.
        c.download();
        assertEquals 404, c.response.getStatus()

        // task set to validate.
        openInstruction.task.setStatusCode(600)
        c.params.id = openInstruction.getId()
        c.download();
        assertEquals 403, c.response.getStatus()
    }
}
