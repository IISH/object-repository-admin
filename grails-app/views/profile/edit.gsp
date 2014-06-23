<%@ page import="org.objectrepository.instruction.Profile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'profile.label', default: 'Profile')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<a href="#edit-profile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="show" action="show" id="${profileInstance.id}"><g:message code="default.show.label"
                                                                                     args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:profileInstance]"/>

<div id="edit-profile" class="content scaffold-edit" role="main">
    <g:form mapping="namingAuthority" params="[na:params.na, id:profileInstance.id, version:profileInstance?.version]" method="PUT">
        <fieldset class="form">
            <ol class="property-list profile"><g:render template="/layouts/profileInstructionForm"
                                                        model="[instance:profileInstance]"/></ol>
        </fieldset>
        <fieldset class="buttons">
            <g:actionSubmit class="save" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>
