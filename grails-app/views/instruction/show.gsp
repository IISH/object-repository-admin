<%@ page import="org.objectrepository.instruction.Instruction" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <g:set var="entityName" value="${message(code: 'instruction.label', default: 'Instruction')}"/>
    <title></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="list" action="list"><g:message code="default.list.label"
                                                          args="[entityName]"/></g:link></li>
        <li><g:link mapping="namingAuthority" class="list" controller="stagingfile"
                    params="[na:params.na,orid: instructionInstance.id]"><g:message code="default.files.label"
                                                                       default="Show files"/></g:link></li>
        <g:render template="/layouts/services" model="[instance: instructionInstance]"/>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: instructionInstance, flash: flash]"/>

<div id="show-instruction" class="content scaffold-instruction" role="main">

    <g:render template="showremote" model="[instructionInstance: instructionInstance]"/>

    <ol class="property-list profile">
        <li class="fieldcontain">
            <span id="fileSetAlias-label" class="property-label"><g:message
                    code="instruction.fileSetAlias.label" default="Fileset"/></span>
            <span class="property-value" aria-labelledby="action-label"><g:fieldValue
                    bean="${instructionInstance}" field="fileSetAlias"/></span>
        </li>
        <li class="fieldcontain">
            <span id="label-label" class="property-label"><g:message code="instruction.label.label"
                                                                     default="Label"/></span>
            <span class="property-value" aria-labelledby="action-label"><g:fieldValue
                    bean="${instructionInstance}" field="label"/></span>
        </li>
        <li class="fieldcontain">
            <span id="objid-label" class="property-label"><g:message code="instruction.objid.label"
                                                                     default="METS object identifier"/></span>
            <span class="property-value" aria-labelledby="objid-label"><g:fieldValue
                    bean="${instructionInstance}" field="objid"/></span>
        </li>
        <g:render template="/layouts/profileInstructionShow" model="[instance: instructionInstance]"/>
    </ol>

    <div class="buttons">
        <g:form mapping="namingAuthority" params="[na:params.na, id:instructionInstance?.id]">
            <g:if test="${!instructionInstance.ingesting && (instructionInstance.task.statusCode <= 300 || instructionInstance.task.statusCode > 699)}">
                <g:if test="${(instructionInstance.task.statusCode == 0 || instructionInstance.task.statusCode > 699)}">
                    <span class="button"><g:actionSubmit class="edit" action="edit"
                                                         value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
                </g:if>
            </g:if>
            <g:else>
                <p>This instruction's attributes are now frozen. They cannot be changed whilst a task is in progress until it is completed.</p>
            </g:else>

            <span class="button"><g:actionSubmit
                    class="delete" action="delete"
                    value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                    onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
        </g:form>
    </div>
</div>

<g:formRemote name="listremote"
              update="updateList"
              url="[mapping:'namingAuthority', action:'showremote', params: [na:params.na,id: instructionInstance.id]]"/>

</body>
</html>
