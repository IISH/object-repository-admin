package org.objectrepository.orfiles

import org.objectrepository.security.Bucket
import org.objectrepository.security.Policy
import org.objectrepository.security.User
import org.objectrepository.util.OrUtil
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.provider.OAuth2Authentication

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
        final String access
        if (fileInstance.metadata.embargo?.length() == 10 && Date.parse('yyyy-MM-dd', fileInstance.metadata.embargo) > new Date()) {
            access = (fileInstance.metadata.embargoAccess) ?: 'closed'
        } else {
            access = fileInstance.metadata.access
        }
        _getPolicy(na, access)
    }

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
            setPolicy(key, policy)
        }
        policy
    }

    void setPolicy(String key, Policy policy) {
        policies.put(key, policy)
    }

    def hasAccess(def access, def na, def pids = null) {
        if (access == "open" || springSecurityService.hasNa(na)) return true
        hasPid(pids)
    }

    /**
     * hasPid
     *
     * Detects if the user is allowed to see the resource as if it has an access=open status
     * Returns a userInstance with the downloads upped.
     *
     * @param pid
     * @return userInstance
     */
    private def hasPid(def pids) {
        if (pids &&
                springSecurityService.authentication instanceof OAuth2Authentication) {
            def userInstance = User.findByUsernameAndUseFor(springSecurityService.principal, "dissemination")
            def date = new Date()
            for (String pid : pids) {
                def resource = userInstance.resources.find {
                    (it.pid == pid || it.pid[-1] == '*' && pid.startsWith(it.pid[0..-2])) &&
                            (it.downloadLimit < 1 || (it.downloads / it.interval < it.downloadLimit)) &&
                            (!it.expirationDate || it.expirationDate > date)
                }
                if (resource) {
                    resource.downloads++
                    return userInstance
                }
            }
        }
    }
}
