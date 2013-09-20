<%@ page import="org.objectrepository.security.UserResource" %>
<g:set var="entityName" value="UserResource"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="list" action="list" id="userInstance.id"><g:message
                code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="create" action="create" id="userInstance.id"><g:message
                code="default.new.label" args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: userResourceInstance]"/>
<div id="edit-userResource" class="content scaffold-edit" role="main">
    <h1><g:message code="default.edit.label" args="[entityName]"/></h1>
    <g:hasErrors bean="${userResourceInstance}">
        <ul class="errors" role="alert">
            <g:eachError bean="${userResourceInstance}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </g:hasErrors>
    <g:form mapping="namingAuthority" params="[na: params.na]" method="post" id="${userInstance.id}">
        <fieldset class="form">
            <g:render template="form"/>
        </fieldset>
        <fieldset class="buttons">
            <g:actionSubmit class="save" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"/>
            <g:actionSubmit class="delete" action="delete"
                            value="${message(code: 'default.button.delete.label', default: 'Delete')}" formnovalidate=""
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
        </fieldset>
    </g:form>
</div>

</body>
</html>
