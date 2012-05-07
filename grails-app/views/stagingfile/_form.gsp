<%@ page import="org.objectrepository.instruction.Stagingfile" %>

<div class="fieldcontain ${hasErrors(bean: stagingfileInstance, field: 'action', 'error')} ">
    <label for="action1">
        <g:message code="Stagingfile.action.label" default="Action"/>
    </label>
    <g:select name="action1" from="${grailsApplication.config.action}" value="${stagingfileInstance?.action}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: stagingfileInstance, field: 'access', 'error')} ">
    <label for="access">
        <g:message code="Stagingfile.access.label" default="Access"/>
    </label>
    <g:select from="${policyList}" name="access" value="${stagingfileInstance.access}" />
</div>