package org.objectrepository.security

import org.bson.types.ObjectId

/**
 * Policy
 *
 * Records all the defaults of a OR instruction.
 * For the workflow per file, we declare a workflow collection of tasks.
 */
class Policy {

    ObjectId id
    String na
    String access
    List<Bucket> buckets

    public String getAccessForBucket(String bucket) {
        buckets.find {
            it.bucket == bucket
        }.access
    }

    static mapping = {
        access(index: true, unique: true)
    }
    static embedded = ['buckets']

    static mapWith = "mongo"
}
