package org.objectrepository.administration

import org.bson.types.ObjectId

/**
 * Globals
 *
 * Contains the generic elements for the Stagingfile (most that is), Profile and Instruction instances
 * As null values from the Stagingfile are supplied by corresponding attributes from Instruction; and
 * Instruction receives null substitute values from the Profile as well; we use a dynamic way to write this
 * in the Bootstrap.
 *
 * Unfortunately, because of
 * http://jira.codehaus.org/browse/GROOVY-4473
 * An inherited class cannot use the delegate.@{$fieldName} attribute to directly get the abstract attribute.
 * That is why for now, we will not use inheritance. Once this Groovy issue is resolved we can apply the Globals class
 * with the -@ accessor
 *
 * @author: Lucien van Wouw <lwo@iisg.nl>
 */
abstract class Globals {

    ObjectId id

    /*
   String na
   String action // = "upsert"
   String access // = "closed"
   String contentType // = "application/octet-stream"
   String resolverBaseUrl // = "http://hdl.handle.net/"
   String autoGeneratePIDs // = "none"
   Boolean autoIngestValidInstruction // = false
   String pidwebserviceEndpoint
   String pidwebserviceKey
    */
}
