package org.objectrepository.instruction

import org.bson.types.ObjectId
import org.objectrepository.util.OrUtil

class Profile {

    // Move these attributes to Globals ( see the comment therein )
    ObjectId id
    String na
    String action = "upsert"
    String access = "closed"
    String embargo
    String embargoAccess
    String label = "enter descriptive tag or title"
    String contentType = "application/octet-stream"
    String resolverBaseUrl = "http://hdl.handle.net/"
    String autoGeneratePIDs = "none"
    Boolean autoIngestValidInstruction = false
    Boolean deleteCompletedInstruction = false
    Boolean replaceExistingDerivatives = false
    String pdfLevel = "level2"
    String pidwebserviceEndpoint
    String pidwebserviceKey
    String notificationEMail
    List<String> plan

    // End move

    Profile() {
        pidwebserviceEndpoint = domainClass.grailsApplication.config.pidwebservice.endpoint
        plan = OrUtil.availablePlans(domainClass.grailsApplication.config.plans)
    }

    static constraints = {
        access(blank: false)
        embargo(nullable: true,  matches:'[0-9]{4}-[0-1][0-9]-[0-3][0-9]')
        embargoAccess(nullable: true)
        notificationEMail(nullable: true)
        contentType(blank: false)
        resolverBaseUrl(blank: false)
        action(inList: ['upsert', 'add', 'update', 'delete'])
        autoGeneratePIDs(inList: ['none', 'uuid', 'lid', 'filename2pid', 'filename2lid'])
        pdfLevel(inList:['master','level1','level2','level3'])
        pidwebserviceEndpoint(nullable: true)
        pidwebserviceKey(nullable: true, matches: '[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}')
    }

    static embedded = ['plan']

    static mapWith = "mongo"
}
