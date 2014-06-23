<%@ page import="org.objectrepository.util.OrUtil" %>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'action', 'error')} ">
    <label for="action1">
        <g:message code="profile.action.label" default="Action"/>
    </label>
    <g:select name="action1" from="${grailsApplication.config.action}" value="${instance.action}"/>
    <g:render template="/profile/approval" model="[instance: instance]"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'access', 'error')} ">
    <label for="access">
        <g:message code="profile.access.label" default="Access"/>
    </label>
    <g:select name="access" from="${policyList}" value="${instance.access}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'embargo', 'error')} ">
    <label for="embargo">
        <g:message code="profile.embargo.label" default="Embargo"/>
    </label>
    <g:textField name="embargo" value="${instance.embargo}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'embargoAccess', 'error')} ">
    <label for="embargoAccess">
        <g:message code="profile.embargoAccess.label" default="Embargo access"/>
    </label>
    <g:select name="embargoAccess" from="${policyList}" value="${instance.embargoAccess}"/>
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

<div class="fieldcontain ${hasErrors(bean: instance, field: 'deleteCompletedInstruction', 'error')} ">
    <label for="deleteCompletedInstruction">
        <g:message code="profile.deleteCompletedInstruction.label" default="Delete a completed instruction"/>

    </label>
    <g:checkBox name="deleteCompletedInstruction" value="${instance.deleteCompletedInstruction}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'replaceExistingDerivatives', 'error')} ">
    <label for="replaceExistingDerivatives">
        <g:message code="profile.replaceExistingDerivatives.label" default="Replace existing derivatives"/>

    </label>
    <g:checkBox name="replaceExistingDerivatives" value="${instance.replaceExistingDerivatives}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: instance, field: 'pdfLevel', 'error')} ">
    <label for="pdfLevel">
        <g:message code="profile.pdfLevel.label" />
    </label>
    <g:select name="pdfLevel" from="${grailsApplication.config.pdfLevel}"
              value="${instance.pdfLevel}"/>
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

<div class="fieldcontain ${hasErrors(bean: instance, field: 'notificationEMail', 'error')} ">
    <label for="notificationEMail">
        <g:message code="notificationEMail.label" default="Notification e-mail"/>
    </label>
    <g:textField name="notificationEMail" value="${instance.notificationEMail}"/>
</div>


<div class="fieldcontain ${hasErrors(bean: instance, field: 'workflow', 'error')} ">
    <label for="workflow"><g:message code="workflow.label"
                                     default="workflow to execute"/></label>

    <div id="workflow" style="padding-left:300px">
        <g:each in="${OrUtil.availablePlans(grailsApplication.config.plans)}" var="plan">
            <g:set var="check" value="${plan in instance.plan}"/>
            <g:checkBox name="plan" value="${plan}" checked="${check}"/> <g:message
                code="${plan}.0.info" default="${plan}"/><br/>
        </g:each>
    </div>
</div>