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
    String embargo
    String embargoAccess
    String contentType
    String resolverBaseUrl
    String autoGeneratePIDs
    Boolean autoIngestValidInstruction
    Boolean deleteCompletedInstruction
    Boolean replaceExistingDerivatives
    String pdfLevel
    String pidwebserviceEndpoint
    String pidwebserviceKey
    String notificationEMail
    String objid
    String resubmitPid
    List<String> plan
    List<Task> workflow = []
    List<String> approval = []

    // End move

    String fileSet
    String label

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
        // ToDo: replace the string parsing with a gmongo find method
        final DBObject query = new BasicDBObject("fileSet", fileSet)
        if (q != null) query.putAll(JSON.parse(q))
        Stagingfile.collection.find(query)
    }

    protected Boolean getApprovalNeeded() {
        approval?.size() < 2 && (action == 'delete' || Stagingfile.countByFileSetAndAction(fileSet, 'delete') != 0)
    }

    /**
     * We will not show the absolute path of the fileSet. Rather only the cpfolder and the main folder name that can be
     * derived from it.
     */
    protected String getFileSetAlias() {
        def file = new File(fileSet)
        "/" + file.parentFile.name + "/" + file.name
    }

    protected int declaredFiles = -1

    protected int getDeclaredFiles() {
        if (declaredFiles == -1) declaredFiles = Stagingfile.countByFileSet(fileSet)
        declaredFiles
    }

    protected Profile _parent = null

    protected def getParent() {
        _parent = (_parent) ?: Profile.findByNa(na)
    }

    static constraints = {
        fileSet(unique: true)
        access(nullable: true)
        embargo(nullable: true,  matches:'[0-9]{4}-[0-9]{2}-[0-9]{2}')
        embargoAccess(nullable: true)
        label(nullable: true)
        notificationEMail(nullable: true)
        action(nullable: true, inList: ['upsert', 'add', 'update', 'delete'])
        contentType(nullable: true)
        resolverBaseUrl(nullable: true)
        autoGeneratePIDs(nullable: true, inList: ['none', 'uuid', 'lid', 'filename2pid', 'filename2lid'])
        pdfLevel(nullable: true, inList: ['master', 'level1', 'level2', 'level3'])
        autoIngestValidInstruction(nullable: true)
        deleteCompletedInstruction(nullable: true)
        replaceExistingDerivatives(nullable: true)
        pidwebserviceEndpoint(nullable: true)
        pidwebserviceKey(nullable: true, matches: '[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}')
        objid(nullable: true)
        resubmitPid(nullable: true)
        plan(nullable: true)
        approval(nullable: true)
    }

    static embedded = ['workflow', 'plan']
    static mapping = {
        fileSet index: true
        na index: true
    }
    static mapWith = "mongo"
}
