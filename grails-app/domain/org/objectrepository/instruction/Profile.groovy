package org.objectrepository.instruction

import org.bson.types.ObjectId
import org.objectrepository.util.OrUtil

class Profile {

    // Move these attributes to Globals ( see the comment therein )
    ObjectId id
    String na
    String action = "upsert"
    String access = "closed"
    String label = "enter descriptive tag or title"
    String contentType = "application/octet-stream"
    String resolverBaseUrl = "http://hdl.handle.net/"
    String autoGeneratePIDs = "none"
    Boolean autoIngestValidInstruction = false
    Boolean deleteCompletedInstruction = false
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
        autoGeneratePIDs(inList: ['none', 'uuid', 'lid', 'filename2pid', 'filename2lid'])
        pidwebserviceEndpoint(nullable: true)
        pidwebserviceKey(nullable: true, matches: '[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}')
    }

    static embedded = ['plan']

    static mapWith = "mongo"
}
