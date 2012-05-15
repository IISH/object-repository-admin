<%@ page import="org.objectrepository.security.User" %>
<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: userInstance]"/>

<div id="list-user" class="content scaffold-list" role="main">
    <table>
        <thead>
        <tr>
            <g:sortableColumn property="username"
                              title="${message(code: 'user.username.label', default: 'Username')}"/>
            <g:sortableColumn property="mail" title="${message(code: 'mail', default: 'Email')}"/>
            <g:sortableColumn property="role" title="${message(code: 'role', default: 'Role')}"/>
            <g:sortableColumn property="enabled"
                              title="${message(code: 'user.enabled.label', default: 'Account Enabled')}"/>
            <sec:ifAnyGranted roles="ROLE_ADMIN"><g:sortableColumn property="na"
                                                                   title="${message(code: 'user.na.label', default: 'Naming authority')}"/></sec:ifAnyGranted>
            <th colspan="3">action</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${userInstanceList}" status="i" var="userInstance">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                <td><g:if test="${userInstance.username == currentUsername}">${fieldValue(bean: userInstance, field: "username")}</g:if><g:else><g:link action="show"
                            id="${userInstance.id}">${fieldValue(bean: userInstance, field: "username")}</g:link></g:else></td>
                <td>${fieldValue(bean: userInstance, field: "mail")}</td>
                <td>${userInstance.roles}</td>
                <td><g:formatBoolean boolean="${userInstance.enabled}"/></td>
                <sec:ifAnyGranted roles="ROLE_ADMIN"><td>${userInstance.na}</td></sec:ifAnyGranted>
                <g:if test="${userInstance.username == currentUsername}">
                    <td colspan="3"></td>
                </g:if>
                <g:else>
                    <td><g:link action="show" id="${userInstance.id}">${message(code: 'default.button.show.label')}</g:link></td>
                    <td><g:link action="edit" id="${userInstance.id}">${message(code: 'default.button.edit.label')}</g:link></td>
                    <td><g:link action="delete" id="${userInstance.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">${message(code: 'default.button.delete.label')}</g:link></td>
                </g:else>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="paginateButtons">
        <g:paginate total="${userInstanceTotal}"/>
    </div>
</div>
</body>
</html>
