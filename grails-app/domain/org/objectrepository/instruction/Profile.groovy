package org.objectrepository.instruction

import org.objectrepository.util.OrUtil

class Profile extends Globals {

    // Move these attributes to Globals ( see the comment therein )
    String na
    String action
    String access
    String contentType
    String resolverBaseUrl
    String autoGeneratePIDs
    Boolean autoIngestValidInstruction
    String pidwebserviceEndpoint
    String pidwebserviceKey
    List<Task> plan

    // End move

    Profile() {
        action = "upsert"
        access = "closed"
        contentType = "application/octet-stream"
        resolverBaseUrl = "http://hdl.handle.net/"
        autoGeneratePIDs = "none"
        pidwebserviceEndpoint = domainClass.grailsApplication.config.pidwebservice.endpoint
        plan = OrUtil.availablePlans(domainClass.grailsApplication.config.workflow)
        autoIngestValidInstruction = false
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
