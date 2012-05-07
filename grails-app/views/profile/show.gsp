<%@ page import="org.objectrepository.instruction.Profile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'profile.label', default: 'Profile')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-profile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<sec:ifAllGranted roles="ROLE_ADMIN"><div class="nav" role="navigation">
    <ul>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div></sec:ifAllGranted>

<g:render template="/layouts/header" model="[instance:profileInstance]"/>

<div id="show-profile" class="content scaffold-show" role="main">

    <ol class="property-list profile"><g:render template="/layouts/profileInstructionShow"
                                                model="[instance:profileInstance]"/></ol>
    <g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${profileInstance?.id}"/>
            <g:link class="edit" action="edit" id="${profileInstance?.id}"><g:message code="default.button.edit.label"
                                                                                      default="Edit"/></g:link>
            <sec:ifAllGranted roles="ROLE_ADMIN"><g:actionSubmit class="delete" action="delete"
                                                                 value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                                 onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></sec:ifAllGranted>
        </fieldset>
    </g:form>
</div>
</body>
</html>
