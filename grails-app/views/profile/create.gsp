<%@ page import="org.objectrepository.instruction.Profile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'profile.label', default: 'Profile')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<a href="#create-profile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:profileInstance]"/>

<div id="create-profile" class="content scaffold-create" role="main">
    <g:form mapping="namingAuthority" params="[na:params.na]" action="save">
        <fieldset class="form">
            <g:render template="/layouts/profileInstructionForm" model="[instance:profileInstance]" />
        </fieldset>
        <fieldset class="buttons">
            <g:submitButton name="create" class="save"
                            value="${message(code: 'default.button.create.label', default: 'Create')}"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>
