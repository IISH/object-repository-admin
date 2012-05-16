<%@ page import="org.objectrepository.util.OrUtil" %>
<div class="header" role="introduction">

    <a target="_blank" style="float:right" href="${grailsApplication.config.grails.serverURL}/doc/latest/guide/${controllerName}.html#${controllerName+actionName}"><img style="width: 30px;" src="${resource(dir: 'images', file: 'or/help_green_64.png')}" alt="Help icon" border="0"/></a>
    <h1><g:message code="${entityName}.${actionName}.title" args="[$args]"/></h1>
    <p><g:message code="${entityName}.${actionName}.intro" args="[args]" /></p>
    <g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if>
    <g:hasErrors bean="${instance}">
        <ul class="errors" role="alert">
            <g:eachError bean="${instance}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </g:hasErrors>
</div>
