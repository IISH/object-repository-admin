package org.objectrepository.util

import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.objectrepository.instruction.Task
import org.objectrepository.security.Bucket
import org.objectrepository.security.Policy

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader

/**
 * OrUtil
 *
 * Utility class of this project. Contains various utility methods.
 *
 * @author Stefan van Wouw <swo@iisg.nl>
 */
class OrUtil {

    final static String or_ns = "http://objectrepository.org/instruction/1.0/"
    final static def filter = ["fileSet", "na", "task"]
    final static def na_pattern = "/?[0-9.]*/"


    /**
     * Stream through a potential xml document
     *
     * @param file
     * @return
     */
    static def hasFSInstruction(File file) {

        final XMLInputFactory xif = XMLInputFactory.newInstance();
        final XMLStreamReader xsr = xif.createXMLStreamReader(file.newReader());
        try {
            return readElement(xsr)
        } catch (Exception e) {
            println(e)
        }
        null
    }

    /**
     * readElement
     *
     * Determine if this is a complete XML document and contains the expected namespace and main attributes.
     * Skips all elements and looks for a final end of document.
     * If found we return the attributes
     *
     * @param xsr
     * @return
     * @throws Exception
     */
    private static Map readElement(XMLStreamReader xsr) throws Exception {

        final Map instruction = null
        while (xsr.hasNext()) {
            final next = xsr.next()
            if (next == XMLStreamReader.START_ELEMENT && !instruction && xsr.localName.contains("instruction")
                    && xsr.namespaceURI.equals(or_ns)) {
                instruction = [:]
                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                    final String key = xsr.getAttributeLocalName(i)
                    if (!(key in filter)) instruction.put(key, xsr.getAttributeValue(i))
                }
            }
        }
        instruction
    }

/**
 * makeOrType
 *
 * Turns the Instruction (without a file) or Stagingfile instance into XML conform the or schema.
 *
 * The xml is not valid when applying an xmlns=or validation, but the end clients do not apply this. Non schema
 * elements will be removed in their marshall
 *
 * @param document
 * @return
 */
    static String makeOrType(document) {

        def orAttributes = [xmlns: "http://objectrepository.org/instruction/1.0/"]
        final LinkedHashMap map = getPropertiesMap(document, true, ['task', 'workflow', 'version', 'label'])
        map << [id: document.id]
        orAttributes.putAll(map)
        orAttributes << [workflow: document.workflow.collect() {
            it.name
        }.join("'")]
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.doubleQuotes = true
        xml.omitNullAttributes = true
        xml.instruction(orAttributes) {
            task {
                identifier document.task.identifier
                name document.task.name
                statusCode document.task.statusCode
            }
        }
        return writer.toString()
    }

/**
 * Utility method for getting all persistent properties mapped to their
 * values of a domain object. Null values will not be returned when
 * noNullValues is true.
 * @param object A domain object to get the property map of.
 * @param noNullValues When true, does not return null values.
 * @return The property map of the domain object,
 * or an empty map when the provided object was null.
 */
    static def getPropertiesMap(def object, def noNullValues = true, def filter) {

        def map = [:]
        if (!object) return map

        new DefaultGrailsDomainClass(object.class).persistentProperties.each {p ->
            def key = p.name
            if (!(key in filter)) {
                def value = object[key]
                if (!noNullValues || value)
                    map.put(key, value)
            }
        }
        map
    }

    static camelCase(def map) {

        map.inject("") {acc, val ->
            acc + val[0].toUpperCase() + val.substring(1)
        }
    }

    public static boolean hasTask(def workflow, Task task) {

        hasTask(workflow, task.name)
    }

    public static boolean hasTask(def workflow, String name) {

        (workflow.find {
            it.name == name
        })
    }

    static boolean removeFirst(List list) {
        (emptyList(list)) ? false : list.remove(list.first())
    }

    static def takeFirst(List list) {
        (emptyList(list)) ? null : list.first()
    }

    static boolean emptyList(def list) {
        (!list || list.size() == 0)
    }

/**
 * availableWorkflows
 *
 * We cannot use the findResults method. It does not seem to work on the remote server.
 *
 * @param workflow
 * @return
 */
    static def availableWorkflows(def workflow) {
        def list = []
        workflow.each {
            if (it.key.startsWith("Stagingfile")) {
                list << new Task(name: it.key, info: "Default workflow")
            }
        }
        list
    }

    static void availablePolicies(String na, def accessMatrix) {
        accessMatrix.each { it ->
            def buckets = it.value.collect { new Bucket(it) }
            Policy.findByAccessAndNa(it.key, na) ?: new Policy(na: na, access: it.key, buckets: buckets).save(failOnError: true)
        }
    }

    static void putAll(def document, Map instruction) {
        instruction.each {
            if (it.key == 'workflow') {
                document.workflow = it.value.split(',').collect { String name ->
                    new Task(name: name, info: 'Added by a xml instruction')
                }
            } else {
                document.setProperty(it.key, it.value)
            }
        }
    }

    static String getNa(String pid) {
        def m = pid =~ na_pattern
        String na = m[0]
        na.replace(".", "_").replace("/", "")
    }
}
