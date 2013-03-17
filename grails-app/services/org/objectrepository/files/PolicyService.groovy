package org.objectrepository.files

import org.objectrepository.security.Bucket
import org.objectrepository.security.Policy

class PolicyService {

    static transactional = false

    def grailsApplication
    def springSecurityService

    private policies = [:]

    /**
     * Retrieves the access matrix for the desired na and status and caches its result
     *
     * @param filesInstance
     * @return
     */
    Policy getPolicy(def fileInstance, def cache = null) {
        if (cache == 'no') policies.clear()
        String na = fileInstance.metadata.na
        String access = fileInstance.metadata.access
        String key = na + "." + access
        def policy = policies[key]
        if (!policy) {
            policy = Policy.findByNaAndAccess(na, access)
            if (!policy) {
                access = "closed" // apply the default policy
                def buckets = grailsApplication.config.accessMatrix[access].collect {
                    new Bucket(it)
                }
                policy = new Policy(na: na, access: access, buckets: buckets).save(failOnError: true)
            }
            setPolicy(key, policy)
        }
        policy
    }

    void setPolicy(String key, Policy policy) {
        policies.put(key, policy)
    }

    boolean denied(def access, def na) {
        !springSecurityService.hasRole('ROLE_ADMIN') && access != "open" && !springSecurityService.hasValidNa(na)
    }
}
