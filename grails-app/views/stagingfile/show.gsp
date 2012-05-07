<%@ page import="org.objectrepository.instruction.Stagingfile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'Stagingfile.label', default: 'Stagingfile')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-Stagingfile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                  default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="show" controller="instruction" action="show"
                    id="${stagingfileInstance.parent.id}"><g:message code="instruction.files"
                                                                     default="Show instruction"/></g:link></li>
        <li><g:link class="list" action="list" params="[orid:stagingfileInstance.parent.id]"><g:message
                code="default.files.label" default="Show files"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:stagingfileInstance]"/>

<g:if test="${stagingfileInstance.workflow.size() > 0}">
    <g:render template="showremote" model="[stagingfileInstance:stagingfileInstance]"/>
</g:if>

<div id="show-Stagingfile" class="content scaffold-show" role="main"><ol class="property-list Stagingfile">

    <li class="fieldcontain">
        <span id="location-label" class="property-label"><g:message code="Stagingfile.location.label"
                                                                    default="Location"/></span>
        <span class="property-value" aria-labelledby="location-label"><g:fieldValue bean="${stagingfileInstance}"
                                                                                    field="location"/></span>
    </li>

    <li class="fieldcontain">
        <span id="pid-label" class="property-label"><g:message code="Stagingfile.pid.label" default="Pid"/></span>
        <span class="property-value" aria-labelledby="pid-label"><g:fieldValue bean="${stagingfileInstance}"
                                                                               field="pid"/>
    </li>

    <li class="fieldcontain">
        <span id="action-label" class="property-label"><g:message code="Stagingfile.action.label"
                                                                  default="Action"/></span>
        <span class="property-value" aria-labelledby="action-label"><g:fieldValue bean="${stagingfileInstance}"
                                                                                  field="action"/></span>
    </li>

    <li class="fieldcontain">
        <span id="access-label" class="property-label"><g:message code="Stagingfile.access.label"
                                                                  default="Access "/></span>
        <span class="property-value" aria-labelledby="access-label"><g:fieldValue
                bean="${stagingfileInstance}" field="access"/></span>
    </li>

    <g:if test="${stagingfileInstance.lid}">
        <li class="fieldcontain">
            <span id="lid-label" class="property-label"><g:message code="Stagingfile.lid.label" default="Lid"/></span>
            <span class="property-value" aria-labelledby="lid-label"><g:fieldValue bean="${stagingfileInstance}"
                                                                                   field="lid"/></span>
        </li>
    </g:if>

    <li class="fieldcontain">
        <span id="md5-label" class="property-label"><g:message code="Stagingfile.md5.label" default="Md5"/></span>

        <span class="property-value" aria-labelledby="md5-label"><g:fieldValue bean="${stagingfileInstance}"
                                                                               field="md5"/></span>
    </li>

    <li class="fieldcontain">
        <span id="contentType-label" class="property-label"><g:message code="Stagingfile.contentType.label"
                                                                       default="contentType"/></span>
        <span class="property-value" aria-labelledby="contentType-label"><g:fieldValue bean="${stagingfileInstance}"
                                                                                       field="contentType"/></span>
    </li>

</ol>

    %{--<g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${stagingfileInstance.id}"/>
            <g:link class="edit" action="edit" id="${stagingfileInstance.id}"><g:message
                    code="default.button.edit.label" default="Edit"/></g:link>
        </fieldset>
    </g:form>--}%

</div>

<g:formRemote name="listremote" update="updateList" url="[action:'showremote', params:[id:stagingfileInstance.id]]"/>

</body>
</html>
