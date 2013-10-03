package org.objectrepository.orfiles

import org.objectrepository.security.Bucket
import org.objectrepository.security.Policy
import org.objectrepository.security.User

class PolicyService {

    static transactional = false

    def grailsApplication
    def springSecurityService

    private policies = [:]

    /**
     * getPolicy
     *
     * Retrieves the access policy as defined in the metadata.access element of the file.
     * Applies the embargo if set, to switch to a different access policy as defined in the metadata.embargoAccess element
     *
     * @param filesInstance
     * @return
     */
    Policy getPolicy(def fileInstance, def cache = null) {
        if (cache == 'no') policies.clear()
        String na = fileInstance.metadata.na
        final String access
        if (fileInstance.metadata.embargo?.length() == 10 && Date.parse('yyyy-MM-dd', fileInstance.metadata.embargo) < new Date()) {
            access = (fileInstance.metadata.embargoAccess) ?: 'closed'
        } else {
            access = fileInstance.metadata.access
        }
        _getPolicy(na, access)
    }

    /**
     *  _getPolicy
     *
     *  Retrieves an access policy and caches it.
     *  Default to a closed access policy when the policy is not known
     *
     * @param na Naming authority
     * @param access Access policy name
     * @return The access policy suitable to the file
     */
    Policy _getPolicy(String na, String access) {
        String key = na + "." + access
        def policy = policies[key]
        if (!policy) {
            policy = Policy.findByNaAndAccess(na, access)
            if (!policy) {
                access = 'closed'
                def buckets = grailsApplication.config.accessMatrix[access].collect {
                    new Bucket(it)
                }
                policy = new Policy(na: na, access: access, buckets: buckets).save(failOnError: true)
            }
            cachePolicy(key, policy)
        }
        policy
    }

    /**
     * cachePolicy
     *
     * Adds an access policy to the memory cache
     *
     * @param key Cache key
     * @param policy The policy to cache
     */
    void cachePolicy(String key, Policy policy) {
        policies.put(key, policy)
    }

    /**
     * hasAccess
     *
     * Detects if the user is allowed to see the resource as if it has an access=open status
     * Returns a true or a userInstance with the downloads upped. Null if no access is allowed.
     *
     * @param access Access status: 'open', 'restricted' or 'closed'
     * @param na Prefix of the PID
     * @param pids PID value
     * @return Array of [status:http status, level:open|restricted|closed, user:null|userInstance]
     *
     */
    def hasAccess(def fileInstance, def bucket = 'master', def cache = null) {
        final def policy = getPolicy(fileInstance, cache)
        final String level = policy.getAccessForBucket(bucket)
        final String na = fileInstance.metadata.na
        if (level == "open" || springSecurityService.hasNa(na)) return [status: 200, level: level]

        if (springSecurityService.authentication.authorities*.authority.find {
            it == "ROLE_OR_DISSEMINATION_all_" + na ||
                    it == "ROLE_OR_DISSEMINATION_" + policy.access + "_" + na
        })
            return [status: 200, level: level]

        def userInstance = User.findByUsernameAndNa(springSecurityService.principal, na)
        if (userInstance) {
            def date = new Date()
            def pids = [fileInstance.metadata.pid, fileInstance.metadata.objid]
            for (String pid : pids) {
                def resource = userInstance.resources.find {
                    (it.pid == pid/* || it.pid[-1] == '*' && pid.startsWith(it.pid[0..-2])*/) &&
                            (it.downloadLimit < 1 || (it.downloads < it.downloadLimit)) &&
                            (!it.expirationDate || it.expirationDate > date)
                }
                if (resource) {
                    if (bucket in resource.buckets) resource.downloads++
                    return [status: 200, level: level, user: userInstance]
                }
            }
        }

        [status: 401, level: level]
    }

}