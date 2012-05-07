package org.objectrepository.files

import org.objectrepository.security.Policy
import org.objectrepository.security.Bucket
import com.sun.accessibility.internal.resources.accessibility

class PolicyService {

    static transactional = false

    def grailsApplication

    private policies = [:]

    /**
     * Retrieves the access matrix for the desired na and status and cache its result
     *
     * @param filesInstance
     * @return
     */
    def getPolicy(Files filesInstance) {
        String key = filesInstance.na + "." + filesInstance.access
        Policy policy = policies[key]
        if (!policy) {
            policy = Policy.findByNaAndAccess(filesInstance.na, filesInstance.access)
            if (!policy){
                def access = "closed" // apply the default policy
                def buckets = grailsApplication.config.accessMatrix[access].collect{
                    new Bucket( it )
                }
                policy = new Policy(na:filesInstance.na, access:access, buckets: buckets).save(failOnError: true)
            }
            setPolicy(filesInstance.na, policy)
        }
        policy
    }

    void setPolicy(String na, Policy policy) {
        String key = na + "." + policy.access
        policies.put(key, policy)
    }
}
