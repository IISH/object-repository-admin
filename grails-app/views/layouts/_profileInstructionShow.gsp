<%@ page import="org.objectrepository.util.OrUtil" %>
        <sec:ifAllGranted roles="ROLE_ADMIN">
            <g:if test="${instance?.na}">
                <li class="fieldcontain">
                    <span id="na-label" class="property-label"><g:message code="profile.na.label" default="Na"/></span>

                    <span class="property-value" aria-labelledby="na-label"><g:fieldValue bean="${instance}"
                                                                                          field="na"/></span>
                </li>
            </g:if>
        </sec:ifAllGranted>

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
            <span id="workflow-label" class="property-label"><g:message code="workflow.label"
                                                                        default="Services to execute"/></span>
            <span class="property-value" aria-labelledby="workflow-label">
                <g:each in="${OrUtil.availablePlans(grailsApplication.config.workflow)}" var="plan">
                    <g:set var="check" value="${plan in instance.plan}"/>
                    <g:message code="${plan}.0.info"
                               default="${plan}"/>: <strong>${check ? "enabled" : "skip"}</strong><br/>
                </g:each>
            </span>
        </li>