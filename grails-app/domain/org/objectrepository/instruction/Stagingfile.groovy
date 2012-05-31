package org.objectrepository.instruction

class Stagingfile extends Globals {

    // Move these attributes to Globals ( see the comment therein )
    String na
    String action
    String access = "closed"
    String contentType
    String md5
    Long length = 0
    String resolverBaseUrl
    String autoGeneratePIDs
    Boolean autoIngestValidInstruction
    String pidwebserviceEndpoint
    String pidwebserviceKey
    List<Task> workflow
    List<Task> failed
    // End move

    String pid
    String fileSet
    String lid
    String location
    Task task

    protected def cacheTask = null

    protected Instruction _parent = null

    protected def setParent(Instruction instruction) {
        _parent = instruction
    }

    protected def getParent() {
        _parent = (_parent) ?: Instruction.findByFileSet(fileSet)
    }

    protected boolean change = false

    static constraints = {
        action(nullable: true)
        access(nullable: true)
        contentType(nullable: true)
        lid(nullable: true)
        task(nullable: true)
        length(nullable: true)
        na(nullable: true)
        resolverBaseUrl(nullable: true)
        autoGeneratePIDs(nullable: true)
        autoIngestValidInstruction(nullable: true)
        pidwebserviceEndpoint(nullable: true)
        pidwebserviceKey(nullable: true)
        workflow(nullable: true)
        failed(nullable: true)
    }

    static embedded = ['task', 'workflow', 'failed']
    static mapWith = "mongo"
    static mapping = {
        fileSet index: true
    }
}

