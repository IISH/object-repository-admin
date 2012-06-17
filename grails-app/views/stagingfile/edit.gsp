<%@ page import="org.objectrepository.instruction.Stagingfile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'Stagingfile.label', default: 'Stagingfile')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<a href="#edit-Stagingfile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                  default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="show" controller="instruction" action="show"
                    id="${stagingfileInstance.parent.id}"><g:message code="instruction.files"
                                                                          default="Show instruction"/></g:link></li>
        <li><g:link class="list" action="list" id="${stagingfileInstance.parent.id}"><g:message
                code="default.files.label" default="Show files"/></g:link></li>
        <li><g:link class="show" action="show" id="${stagingfileInstance.id}"><g:message
                code="default.files.label" default="Show staging file"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:stagingfileInstance]"/>

<div id="edit-Stagingfile" class="content scaffold-edit" role="main">
    <g:form method="post">
        <g:hiddenField name="id" value="${stagingfileInstance?.id}"/>
        <g:hiddenField name="version" value="${stagingfileInstance?.version}"/>
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
