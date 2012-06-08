package org.objectrepository.instruction

import org.bson.types.ObjectId
import org.objectrepository.util.OrUtil

class Profile {

    // Move these attributes to Globals ( see the comment therein )
    ObjectId id
    String na
    String action = "upsert"
    String access = "closed"
    String contentType = "application/octet-stream"
    String resolverBaseUrl = "http://hdl.handle.net/"
    String autoGeneratePIDs = "none"
    Boolean autoIngestValidInstruction = false
    String pidwebserviceEndpoint
    String pidwebserviceKey
    List<String> plan

    // End move

    Profile() {
        pidwebserviceEndpoint = domainClass.grailsApplication.config.pidwebservice.endpoint
        plan = OrUtil.availablePlans(domainClass.grailsApplication.config.plans)
    }

    static constraints = {
        access(blank: false)
        contentType(blank: false)
        resolverBaseUrl(blank: false)
        action(inList: ['upsert', 'add', 'update', 'delete'])
        autoGeneratePIDs(inList: ['none', 'uuid', 'lid', 'filename'])
        pidwebserviceEndpoint(nullable: true)
        pidwebserviceKey(nullable: true)
    }

    static embedded = ['plan']

    static mapWith = "mongo"
}
