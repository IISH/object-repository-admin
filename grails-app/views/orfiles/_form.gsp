<%@ page import="org.objectrepository.files.Files" %>

<div class="fieldcontain ${hasErrors(bean: filesInstance, field: 'access', 'error')} ">
    <label for="access">
        <g:message code="files.access.label" default="Access"/>
    </label>
    <g:select from="${policyList}" name="access" value="${filesInstance.access}" />
</div>

<div class="fieldcontain ${hasErrors(bean: filesInstance, field: 'label', 'error')} ">
    <label for="label">
        <g:message code="files.label.label" default="Label"/>
    </label>
    <g:textField name="label" value="${filesInstance?.label}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: filesInstance, field: 'resolverBaseUrl', 'error')} ">
    <label for="label">
        <g:message code="files.resolverBaseUrl.label" default="Resolver baseurl"/>
    </label>
    <g:textField name="resolverBaseUrl" value="${filesInstance.resolverBaseUrl}"/>
</div>