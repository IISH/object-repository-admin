<%@ page import="org.objectrepository.util.OrUtil" %>

<li class="fieldcontain">
    <span id="action-label" class="property-label"><g:message
            code="profile.action.label" default="Action"/></span>
    <span class="property-value" aria-labelledby="action-label"><g:fieldValue
            bean="${instance}" field="action"/></span>

</li>

<li class="fieldcontain">
    <span id="access-label" class="property-label"><g:message
            code="profile.access.label" default="Access"/></span>

    <span class="property-value" aria-labelledby="access-label"><g:fieldValue
            bean="${instance}" field="access"/></span>

</li>

<li class="fieldcontain">
    <span id="contentType-label" class="property-label"><g:message
            code="profile.contentType.label" default="Contenttype"/></span>

    <span class="property-value" aria-labelledby="contentType-label"><g:fieldValue
            bean="${instance}" field="contentType"/></span>

</li>

<li class="fieldcontain">
    <span id="autoGeneratePIDs-label" class="property-label"><g:message
            code="profile.autoGeneratePIDs.label" default="Auto Generate PID s"/></span>

    <span class="property-value" aria-labelledby="autoGeneratePIDs-label"><g:fieldValue
            bean="${instance}" field="autoGeneratePIDs"/></span>

</li>

<li class="fieldcontain">
    <span id="autoIngestValidInstruction-label" class="property-label"><g:message
            code="profile.autoIngestValidInstruction.label" default="Auto Ingest Valid Instruction"/></span>

    <span class="property-value" aria-labelledby="autoIngestValidInstruction-label"><g:formatBoolean
            boolean="${instance?.autoIngestValidInstruction}"/></span>

</li>

<li class="fieldcontain">
    <span id="deleteCompletedInstruction-label" class="property-label"><g:message
            code="profile.deleteCompletedInstruction.label" default="Delete a completed instruction"/></span>

    <span class="property-value" aria-labelledby="deleteCompletedInstruction-label"><g:formatBoolean
            boolean="${instance?.deleteCompletedInstruction}"/></span>

</li>

<li class="fieldcontain">
    <span id="replaceExistingDerivatives-label" class="property-label"><g:message
            code="profile.replaceExistingDerivatives.label" default="Replace existing derivatives"/></span>

    <span class="property-value" aria-labelledby="replaceExistingDerivatives-label"><g:formatBoolean
            boolean="${instance?.replaceExistingDerivatives}"/></span>

</li>

<li class="fieldcontain">
    <span id="pdfLevel-label" class="property-label"><g:message
            code="profile.pdfLevel.label" default="Derivative level for PDF rendering"/></span>

    <span class="property-value" aria-labelledby="pdfLevel-label"><g:fieldValue
            bean="${instance}" field="pdfLevel"/></span>

</li>

<li class="fieldcontain">
    <span id="resolverBaseUrl-label" class="property-label"><g:message code="profile.resolverBaseUrl.label"
                                                                       default="Resolver Base Url"/></span>

    <span class="property-value" aria-labelledby="resolverBaseUrl-label"><g:fieldValue
            bean="${instance}" field="resolverBaseUrl"/></span>

</li>

<li class="fieldcontain">
    <span id="pidWebservice-url-label" class="property-label"><g:message code="pidWebservice.url.label"
                                                                         default="Pid webservice endpoint"/></span>
    <span class="property-value" aria-labelledby="pidWebservice-url-label"><g:fieldValue
            bean="${instance}" field="pidwebserviceEndpoint"/></span>
</li>

<li class="fieldcontain">
    <span id="pidWebservice-key-label" class="property-label"><g:message code="pidWebservice.key.label"
                                                                         default="Pid webservice key"/></span>
    <span class="property-value" aria-labelledby="pidWebservice-key-label"><g:fieldValue
            bean="${instance}" field="pidwebserviceKey"/></span>
</li>

<li class="fieldcontain">
    <span id="notificationEMail-key-label" class="property-label"><g:message code="notificationEMail.label"
                                                                             default="Notification e-mail"/></span>
    <span class="property-value" aria-labelledby="notificationEMail-label"><g:fieldValue
            bean="${instance}" field="notificationEMail"/></span>
</li>

<li class="fieldcontain">
    <span id="workflow-label" class="property-label"><g:message code="workflow.label"
                                                                default="Services to execute"/></span>
    <span class="property-value" aria-labelledby="workflow-label">
        <g:each in="${OrUtil.availablePlans(grailsApplication.config.plans)}" var="plan">
            <g:set var="check" value="${plan in instance.plan}"/>
            <g:message code="${plan}.0.info"
                       default="${plan}"/>: <strong>${check ? "enabled" : "skip"}</strong><br/>
        </g:each>
    </span>
</li>