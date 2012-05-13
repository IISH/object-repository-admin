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

    /**
     * Master files are always closed
     *
     * @return
     */
    protected def beforeChange() {
        buckets.find {
            it.bucket == "master"
        }.access = "closed"

    }

    def beforeUpdate() {
        beforeChange()
    }

    def beforeSave() {
        beforeChange()
    }

    def beforeInsert() {
        beforeChange()
    }

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
