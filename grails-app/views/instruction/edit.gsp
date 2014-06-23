<%@ page import="org.objectrepository.instruction.Instruction" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <g:set var="entityName" value="${message(code: 'instruction.label', default: 'Instruction')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="list" action="list"><g:message
                code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="show" action="show"
                    id="${instructionInstance.id}"><g:message code="default.show.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: instructionInstance, flash: flash]"/>

<div id="edit-instruction" class="content scaffold-edit" role="main">
    <g:form mapping="namingAuthority"
            params="[na: params.na, id: instructionInstance.id, version: instructionInstance?.version]" method="PUT"
            action="edit">
        <ol class="property-list profile">
            <div class="fieldcontain ${hasErrors(bean: instructionInstance, field: 'label', 'error')} ">
                <label for="label">
                    <g:message code="instruction.label.label" default="Label"/>
                </label>
                <g:textField name="label" value="${instructionInstance.label}"/>
            </div>

            <div class="fieldcontain ${hasErrors(bean: instructionInstance, field: 'objid', 'error')} ">
                <label for="objid">
                    <g:message code="instruction.objid.label"/>
                </label>
                <g:textField name="objid" value="${instructionInstance.objid}"/>
            </div>
            <g:render template="/layouts/profileInstructionForm"
                      model="[instance: instructionInstance]"/>
        </ol>


        <fieldset class="buttons">
            <g:actionSubmit class="save" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>