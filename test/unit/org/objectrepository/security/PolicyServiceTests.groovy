package org.objectrepository.security

import grails.test.mixin.TestFor
import grails.util.Environment
import org.objectrepository.orfiles.PolicyService
import org.springframework.security.core.authority.GrantedAuthorityImpl

@TestFor(PolicyService)
class PolicyServiceTests {

    final String na = "00000"
    Policy policyCache
    User userCache

    // What we would expect with the build in policies 'open', 'restricted', 'closed' ( or null ) and administration
    final expectedPolicieResponses = [
            open: ['master': 401, 'level1': 200, 'level2': 200, 'level3': 200],
            restricted: ['master': 401, 'level1': 401, 'level2': 200, 'level3': 200],
            closed: ['master': 401, 'level1': 401, 'level2': 401, 'level3': 401],
            null: ['master': 401, 'level1': 401, 'level2': 401, 'level3': 401],
            administration: ['master': 200, 'level1': 200, 'level2': 200, 'level3': 200]
    ]


    void setUp() {
        GroovyClassLoader classLoader = new GroovyClassLoader(this.class.classLoader)
        ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
        config = slurper.parse(classLoader.loadClass("Config"))

        LinkedHashMap.metaClass.hasNa { na_ ->
            def role = "ROLE_OR_USER_" + na_
            na_ && (role in service.springSecurityService.authentication.authorities*.authority)
        }
        Policy.metaClass.'static'.save << { map ->
            delegate
        }
        policyCache = new Policy(na: na, buckets: [])
        Policy.metaClass.'static'.findByNaAndAccess << { na, access ->
            new Policy(na: na, access: access, buckets: config.accessMatrix[access].collect {
                new Bucket(it)
            })
        }
        User.metaClass.'static'.findByUsernameAndNa << { username, na_ ->
            (userCache?.username == username && userCache?.na == na_) ? userCache : null
        }
    }

    static def metadata(String pid, String access, String embargo = null, String embargoAccess = null) {
        def split = pid.split('/', 2)
        [metadata: [na: split[0], pid: pid, access: access, embargo: embargo, embargoAccess: embargoAccess]]
    }

    void testAnonymousAccess() {
        service.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_ANONYMOUS')]]]

        // We expect with policy open|restricted and closed...
        [
                'open':
                        expectedPolicieResponses.open,
                'restricted':
                        expectedPolicieResponses.restricted,
                'closed':
                        expectedPolicieResponses.closed
        ].each { policy ->
            policy.value.each {
                bucket, status ->
                    assert service.hasAccess(metadata(na + '/some pid', policy.key), bucket).status == status
                    assert service.hasAccess(metadata('12345/some pid', policy.key), bucket).status == status
            }
        }

    }

    void testEmbargoAnonymousAccess() {

        service.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_ANONYMOUS')]]]

        String yesterday = new Date().minus(1).format('yyyy-MM-dd')
        String tomorrow = new Date().plus(1).format('yyyy-MM-dd')

        // Tomorrow we open, restrict and close access... embargo will have no effect
        ['closed', null, 'restricted', 'open'].each { embargoAccess ->
            [
                    'open':
                            expectedPolicieResponses.open,
                    'restricted':
                            expectedPolicieResponses.restricted,
                    'closed':
                            expectedPolicieResponses.closed
            ].each { policy ->
                policy.value.each {
                    bucket, status ->
                        assert service.hasAccess(metadata(na + '/some pid', policy.key, tomorrow, embargoAccess), bucket).status == status
                        assert service.hasAccess(metadata('12345/some other pid', policy.key, tomorrow, embargoAccess), bucket).status == status
                }
            }
        }

        // Since yesterday we have open, restrict and close access... embargo will have an effect
        ['closed', null, 'restricted', 'open'].each { embargoAccess ->
            [
                    'open':
                            expectedPolicieResponses[embargoAccess],
                    'restricted':
                            expectedPolicieResponses[embargoAccess],
                    'closed':
                            expectedPolicieResponses[embargoAccess]
            ].each { policy ->
                policy.value.each {
                    bucket, status ->
                        assert service.hasAccess(metadata(na + '/some pid', policy.key, yesterday, embargoAccess), bucket).status == status
                        assert service.hasAccess(metadata('12345/some other pid', policy.key, yesterday, embargoAccess), bucket).status == status
                }
            }
        }
    }

    void testAdministrationAccess() {
        service.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_USER_' + na)]]]

        [
                'open':
                        expectedPolicieResponses.open,
                'restricted':
                        expectedPolicieResponses.restricted,
                'closed':
                        expectedPolicieResponses.closed
        ].each { policy ->
            policy.value.each {
                bucket, status ->
                    assert service.hasAccess(metadata(na + '/some pid', policy.key), bucket).status == expectedPolicieResponses.administration[bucket]
                    assert service.hasAccess(metadata('12345/some pid', policy.key), bucket).status == status
            }
        }
    }

    void testDisseminationRoleAccess() {

        service.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_DISSEMINATION_' + na)]]]
        [
                open: ['administration', 'restricted', 'closed'],
                restricted: ['administration', 'administration', 'closed'],
                closed: ['administration', 'administration', 'administration']
        ].each {
            service.springSecurityService.authentication.authorities << new GrantedAuthorityImpl('ROLE_OR_DISSEMINATION_' + na + '_' + it.key)
            [
                    'open':
                            expectedPolicieResponses[it.value[0]],
                    'restricted':
                            expectedPolicieResponses[it.value[1]],
                    'closed':
                            expectedPolicieResponses[it.value[2]]
            ].each { policy ->
                policy.value.each {
                    bucket, status ->
                        assert service.hasAccess(metadata(na + '/some pid', policy.key), bucket).status == status
                }
            }
        }

        [
                'open':
                        expectedPolicieResponses.open,
                'restricted':
                        expectedPolicieResponses.restricted,
                'closed':
                        expectedPolicieResponses.closed
        ].each { policy ->
            policy.value.each {
                bucket, status ->
                    assert service.hasAccess(metadata('12345/some pid', policy.key), bucket).status == status
            }
        }
    }

    void testDisseminationLimitedAccess() {

        String principal = 'a username'
        service.springSecurityService = [
                principal: principal,
                authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_DISSEMINATION_' + na)]]]

        final String pid = na + '/' + UUID.randomUUID().toString()
        userCache = new User(username: principal, na: na, resources: [
                new UserResource(pid: pid, downloadLimit: 5)])

        // Same as anonymous view
        [
                'open':
                        expectedPolicieResponses.open,
                'restricted':
                        expectedPolicieResponses.restricted,
                'closed':
                        expectedPolicieResponses.closed
        ].each { policy ->
            policy.value.each {
                bucket, status ->
                    assert service.hasAccess(metadata(na + '/some pid', policy.key), bucket).status == status
                    assert service.hasAccess(metadata('12345/some pid', policy.key), bucket).status == status
            }
        }

        // Only the master download should count. Therefor we put it last
        ['open', 'restricted', 'closed'].each { accessStatus ->
            ['level1', 'level2', 'level3'].each { bucket ->
                service.hasAccess(metadata(pid, accessStatus), bucket)
            }
        }
        assert userCache.resources[0].downloads == 0
        for (int downloads = 1; downloads <= userCache.resources[0].downloadLimit; downloads++) {
            assert service.hasAccess(metadata(pid, 'open'), 'master').user.resources[0].downloads == downloads
        }
        assert service.hasAccess(metadata(pid, 'open'), 'master').status == 401
        assert service.hasAccess(metadata(pid, 'open'), 'level1').status == 200
        assert service.hasAccess(metadata(pid, 'open'), 'level2').status == 200
        assert service.hasAccess(metadata(pid, 'open'), 'level3').status == 200

        userCache.resources[0].downloads = 0
        userCache.resources[0].downloadLimit = 0
        userCache.resources[0].expirationDate = new Date().plus(1) // Tomorrow
        for (int downloads = 1; downloads <= 10; downloads++) {
            final hasAccess = service.hasAccess(metadata(pid, 'open'), 'master')
            assert hasAccess.status == 200
            assert hasAccess.user.resources[0].downloads == downloads
            assert service.hasAccess(metadata(na + '/a pid', 'open'), 'master').status == 401
            assert service.hasAccess(metadata('12345/another pid', 'open'), 'master').status == 401
        }

        userCache.resources[0].expirationDate = new Date().minus(1) // Yesterday. Should be treated as anonymous
        [
                'open':
                        expectedPolicieResponses.open,
                'restricted':
                        expectedPolicieResponses.restricted,
                'closed':
                        expectedPolicieResponses.closed
        ].each { policy ->
            policy.value.each {
                bucket, status ->
                    assert service.hasAccess(metadata(pid, policy.key), bucket).status == status
                    assert service.hasAccess(metadata(na + '/some pid', policy.key), bucket).status == status
                    assert service.hasAccess(metadata('12345/another pid', policy.key), bucket).status == status
            }
        }
    }
}
