<%@ page import="org.objectrepository.security.Policy" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'policy.label', default: 'Policy')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<a href="#edit-policy" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                             default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="list" action="list"><g:message
                code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="create" action="create"><g:message
                code="default.new.label"
                args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: policyInstance]"/>

<div id="edit-policy" class="content scaffold-edit" role="main">
    <g:form mapping="namingAuthority" params="[na: params.na, id: policyInstance?.id, version: policyInstance?.version]"
            method="post">
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
