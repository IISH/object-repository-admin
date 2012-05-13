<%@ page import="org.objectrepository.files.Files" %>
<!doctype html>

<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'files.label', default: 'Files')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<a href="#edit-files" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                            default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link class="show" action="show" id="${filesInstance.id}"><g:message code="default.show.label"
                                                                                   args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:filesInstance]"/>

<div id="edit-files" class="content scaffold-edit" role="main">

    <g:form method="post">
        <g:hiddenField name="id" value="${filesInstance?.id}"/>
        <g:hiddenField name="version" value="${filesInstance?.version}"/>
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
