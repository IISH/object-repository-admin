<%@ page import="org.objectrepository.instruction.Profile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'profile.label', default: 'Profile')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<a href="#edit-profile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <sec:ifAllGranted roles="ROLE_ADMIN"><li><g:link class="list" action="list"><g:message code="default.list.label"
                                                                                               args="[entityName]"/></g:link></li>
            <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                                  args="[entityName]"/></g:link></li></sec:ifAllGranted>
        <li><g:link class="show" action="show" id="${profileInstance.id}"><g:message code="default.show.label"
                                                                                     args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:profileInstance]"/>

<div id="edit-profile" class="content scaffold-edit" role="main">
    <g:form method="post">
        <g:hiddenField name="id" value="${profileInstance.id}"/>
        <g:hiddenField name="version" value="${profileInstance?.version}"/>
        <fieldset class="form">
            <ol class="property-list profile"><g:render template="/layouts/profileInstructionForm"
                                                        model="[instance:profileInstance]"/></ol>
        </fieldset>
        <fieldset class="buttons">
            <g:actionSubmit class="save" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"/>
            <sec:ifAllGranted roles="ROLE_ADMIN"><g:actionSubmit class="delete" action="delete"
                                                                 value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                                 formnovalidate=""
                                                                 onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></sec:ifAllGranted>
        </fieldset>
    </g:form>
</div>
</body>
</html>
