package org.objectrepository.instruction

import org.bson.types.ObjectId

class Stagingfile extends Tasking {

    // Move these attributes to Globals ( see the comment therein )
    ObjectId id
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
    List<Task> workflow = []
    // End move

    String pid
    String fileSet
    String lid
    String location

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
        length(nullable: true)
        na(nullable: true)
        resolverBaseUrl(nullable: true)
        autoGeneratePIDs(nullable: true)
        autoIngestValidInstruction(nullable: true)
        pidwebserviceEndpoint(nullable: true)
        pidwebserviceKey(nullable: true)
    }

    static embedded = ['workflow']
    static mapWith = "mongo"
    static mapping = {
        fileSet index: true
    }
}

