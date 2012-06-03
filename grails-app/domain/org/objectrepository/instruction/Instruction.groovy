package org.objectrepository.instruction

import com.mongodb.BasicDBObject
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.util.JSON
import org.bson.types.ObjectId

/**
 * Instruction
 *
 * Stand alone object. It has children... Stagingfile and Task.
 *
 * We will not add a hasMany relation as it uses the dbref object.
 * Although in itself that may not be a problem; there is the 4MB document limit to consider with many file references
 * reaching that limit easily.
 *
 * Hence we use the fileSet as key to relate the Instruction document and Stagingfile documents
 *
 */
class Instruction extends Tasking {

    // Move these attributes to Globals ( see the comment therein )
    ObjectId id
    String na
    String action
    String access
    String contentType
    String resolverBaseUrl
    String autoGeneratePIDs
    Boolean autoIngestValidInstruction
    String pidwebserviceEndpoint
    String pidwebserviceKey
    List<String> plan
    List<Task> workflow = []

    // End move

    String fileSet
    String label = 'enter descriptive tag or title'

    protected def _services = null
    protected int status = 200
    protected boolean change = false

    def beforeDelete() {
        final DBObject query = new BasicDBObject("fileSet", fileSet)
        Stagingfile.collection.remove(query)
    }

    /**
     * Get the file types starting with fileSet. Returns a cursor because
     * there might be large amounts of files which match t
     *
     * @return
     */
    protected DBCursor findFilesWithCursor() {
        findFilesWithCursorByQuery(null)
    }

    protected DBCursor findFilesWithCursorByQuery(String q) { // Could use a where query...

        final DBObject query = new BasicDBObject("fileSet", fileSet)
        if (q != null) query.putAll(JSON.parse(q))
        Stagingfile.collection.find(query)
    }
    /**
     * We will not show the absolute path of the fileSet. Rather only the cpfolder and the main folder name that can be
     * derived from it.
     */
    protected String getFileSetAlias() {
        def file = new File(fileSet)
        "/" + file.parentFile.name + "/" + file.name
    }

    /**
     * ingest
     *
     * Helper getter for the statusCode
     *
     * @return
     */
    protected String getIngest() {
        if (task.name == 'InstructionIngest' && task.statusCode == 800) return 'working'
        'pending'
    }

    protected Profile _parent = null

    protected def getParent() {
        _parent = (_parent) ?: Profile.findByNa(na)
    }

    static constraints = {
        fileSet(unique: true)
        access(nullable: true)
        action(nullable: true, inList: ['upsert', 'add', 'update', 'delete'])
        contentType(nullable: true)
        resolverBaseUrl(nullable: true)
        autoGeneratePIDs(nullable: true, inList: ['none', 'uuid', 'lid', 'filename2pid', 'filename2lid'])
        autoIngestValidInstruction(nullable: true)
        pidwebserviceEndpoint(nullable: true)
        pidwebserviceKey(nullable: true)
        plan(nullable: true)
    }

    static embedded = ['workflow', 'plan']
    static mapping = {
        fileSet index: true
        na index: true
    }
    static mapWith = "mongo"
}
