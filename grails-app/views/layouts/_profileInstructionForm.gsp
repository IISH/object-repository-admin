<%@ page import="org.objectrepository.util.OrUtil" %><sec:ifAllGranted roles="ROLE_ADMIN"><div
        class="fieldcontain ${hasErrors(bean: instance, field: 'na', 'error')} ">
    <label for="na">
        <g:message code="profile.na.label" default="Na"/>

    </label>
    <g:textField name="na" value="${instance.na}"/>
</div></sec:ifAllGranted>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'action', 'error')} ">
    <label for="action1">
        <g:message code="profile.action.label" default="Action"/>
    </label>
    <g:select name="action1" from="${grailsApplication.config.action}" value="${instance.action}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'access', 'error')} ">
    <label for="access">
        <g:message code="profile.access.label" default="Access"/>
    </label>
    <g:select name="access" from="${policyList}" value="${instance.access}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'contentType', 'error')} ">
    <label for="contentType">
        <g:message code="profile.contentType.label" default="Contenttype"/>
    </label>
    <g:select name="contentType" value="${instance.contentType}"
              from="${grailsApplication.config.contentType}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'autoGeneratePIDs', 'error')} ">
    <label for="autoGeneratePIDs">
        <g:message code="profile.autoGeneratePIDs.label" default="Auto Generate PIDs"/>
    </label>
    <g:select name="autoGeneratePIDs" from="${grailsApplication.config.autoGeneratePIDs}"
              value="${instance.autoGeneratePIDs}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'autoIngestValidInstruction', 'error')} ">
    <label for="autoIngestValidInstruction">
        <g:message code="profile.autoIngestValidInstruction.label" default="Auto Ingest Valid Instruction"/>

    </label>
    <g:checkBox name="autoIngestValidInstruction" value="${instance.autoIngestValidInstruction}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'resolverBaseUrl', 'error')} ">
    <label for="resolverBaseUrl">
        <g:message code="profile.resolverBaseUrl.label" default="Resolver Base Url"/>

    </label>
    <g:textField name="resolverBaseUrl" value="${instance.resolverBaseUrl}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'pidwebserviceEndpoint', 'error')} ">
    <label for="pidwebserviceEndpoint">
        <g:message code="pidWebservice.url.label" default="Pid webservice endpoint"/>
    </label>
    <g:textField name="pidwebserviceEndpoint" value="${instance.pidwebserviceEndpoint}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'pidwebserviceKey', 'error')} ">
    <label for="pidwebserviceKey">
        <g:message code="pidWebservice.key.label" default="Pid webservice key"/>
    </label>
    <g:textField name="pidwebserviceKey" value="${instance.pidwebserviceKey}"/>
</div>


<div class="fieldcontain ${hasErrors(bean: instance, field: 'workflow', 'error')} ">
    <label for="workflow"><g:message code="workflow.label"
                                     default="workflow to execute"/></label>

    <div id="workflow" style="padding-left:300px">
        <g:each in="${OrUtil.availablePlans(grailsApplication.config.workflow)}" var="task">
            <g:set var="check" value="${instance.hasTask(task)}"/>
            <g:checkBox name="plan.${task.name}" checked="${check}"/> <g:message
                code="${task.name}.0.info" default="${task.name}"/><br/>
        </g:each>
    </div>
</div>