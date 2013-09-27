package org.objectrepository.security

import grails.test.mixin.TestFor
import org.objectrepository.orfiles.PolicyService
import org.springframework.security.core.authority.GrantedAuthorityImpl

@TestFor(PolicyService)
class PolicyServiceTests {

    String na = "00000"

    PolicyServiceTests() {
        LinkedHashMap.metaClass.hasNa { na_ ->
            def role = "ROLE_OR_USER_" + na_
            na_ && (role in service.springSecurityService.authentication.authorities*.authority)
        }
    }

    void testAnonymousAccess() {
        service.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_ANONYMOUS')]]]
        assert service.hasAccess("open", '12345', ['12345/some pid'])
        assert !service.hasAccess("restricted", '12345', ['12345/some pid'])
        assert !service.hasAccess("closed", '12345', ['12345/some pid'])
    }

    void testNaAccess() {
        service.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_USER_' + na)]]]
        assert !service.hasAccess("restricted", '12345', ['12345/some pid'])
        assert !service.hasAccess("closed", '12345', ['12345/some pid'])
        assert service.hasAccess("restricted", na, ['00000/some pid'])
        assert service.hasAccess("closed", na, ['00000/some pid'])
    }

    void testScopeOpenAccess() {
        service.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_DISSEMINATION_OPEN_' + na)]]]
        assert !service.hasAccess("restricted", '12345', ['12345/some pid'])
        assert !service.hasAccess("closed", '12345', ['12345/some pid'])
        assert !service.hasAccess("restricted", na, ['00000/some pid'])
        assert !service.hasAccess("closed", na, ['00000/some pid'])
    }

    void testScopeRestrictedAccess() {
        service.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_DISSEMINATION_RESTRICTED_' + na)]]]
        assert !service.hasAccess("restricted", '12345', ['12345/some pid'])
        assert !service.hasAccess("closed", '12345', ['12345/some pid'])
        assert service.hasAccess("restricted", na, ['00000/some pid'])
        assert !service.hasAccess("closed", na, ['00000/some pid'])
    }

    void testScopeClosedAccess() {
        service.springSecurityService = [authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_DISSEMINATION_CLOSED_' + na)]]]
        assert !service.hasAccess("restricted", '12345', ['12345/some pid'])
        assert !service.hasAccess("closed", '12345', ['12345/some pid'])
        assert service.hasAccess("restricted", na, ['00000/some pid'])
        assert service.hasAccess("closed", na, ['00000/some pid'])
    }

    void testScopeLimitedAccess() {

        String principal = 'a username'
        int downloadLimit = 5
        service.springSecurityService = [
                principal: principal,
                authentication: [authorities: [new GrantedAuthorityImpl('ROLE_OR_DISSEMINATION_LIMITED_' + na)]]]

        final User cache = new User(username: principal, resources: [
                new UserResource(pid: na + '/67890', downloadLimit: downloadLimit)])
        User.metaClass.'static'.findByUsername << { username ->
            cache
        }

        assert !service.hasAccess("restricted", '12345', ['12345/some pid'])
        assert !service.hasAccess("closed", '12345', ['12345/some pid'])
        assert !service.hasAccess("restricted", na, ['00000/some pid'])
        assert !service.hasAccess("closed", na, ['00000/some pid'])

        for (int downloads = 1; downloads <= downloadLimit; downloads++) {
            assert !service.hasAccess("closed", na, ['00000/12345'])
            assert service.hasAccess("closed", na, ['00000/67890']).resources[0].downloads == downloads
        }
        !service.hasAccess("closed", na, ['00000/12345'])
        !service.hasAccess("closed", na, ['00000/67890'])

        cache.resources[0].downloadLimit = 0
        !service.hasAccess("closed", na, ['00000/12345'])
        service.hasAccess("closed", na, ['00000/67890'])

        cache.resources[0].expirationDate = new Date().minus(1)
        !service.hasAccess("closed", na, ['00000/12345'])
        !service.hasAccess("closed", na, ['00000/67890'])
        cache.resources[0].expirationDate = new Date().plus(1)
        !service.hasAccess("closed", na, ['00000/12345'])
        service.hasAccess("closed", na, ['00000/67890'])
    }
}
