<%@ page import="org.objectrepository.instruction.Profile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'profile.label', default: 'Profile')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-profile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<g:render template="/layouts/header" model="[instance:profileInstance]"/>

<div id="show-profile" class="content scaffold-show" role="main">
    <ol class="property-list profile"><g:render template="/layouts/profileInstructionShow"
                                                model="[instance:profileInstance]"/></ol>
    <g:form mapping="namingAuthority" params="[na:params.na, id:profileInstance?.id]">
        <fieldset class="buttons">
            <g:link mapping="namingAuthority" params="[na:params.na]" class="edit" action="edit" id="${profileInstance?.id}"><g:message code="default.button.edit.label"
                                                                                      default="Edit"/></g:link>
        </fieldset>
    </g:form>
</div>
</body>
</html>
