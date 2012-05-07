<%@ page import="org.objectrepository.security.Policy" %>

<div class="fieldcontain ${hasErrors(bean: policyInstance, field: 'access', 'error')} ">

    <p><label for="access">
        <g:message code="policy.access.label" default="Access"/>
    </label>
        <g:textField name="access" value="${policyInstance?.access}"/></p>

    <g:each in="${policyInstance.buckets}" var="bucket">
        <p>
            <label for="${bucket.bucket}">
                <g:message code="policy.access.${bucket.bucket}" default="${bucket.bucket}"/>
            </label>
            <g:select from="${accessStatus}" name="${bucket.bucket}" value="${bucket.access}" />
        </p>
    </g:each>
</div>

