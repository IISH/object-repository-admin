<%@ page import="org.objectrepository.security.User" %>
<g:set var="entityName" value="${message(code: 'user.label', default: 'Account')}"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="create" action="create"><g:message code="default.new.label"
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
            <g:sortableColumn property="mail" title="${message(code: 'user.email.label', default: 'Email')}"/>
            <g:sortableColumn property="enabled"
                              title="${message(code: 'user.enabled.label', default: 'Account Enabled')}"/>
            <th colspan="3">action</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${userInstanceList}" status="i" var="userInstance">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                <td><g:link mapping="namingAuthority" params="[na:params.na]" action="show"
                            id="${userInstance.id}">${fieldValue(bean: userInstance, field: "username")}</g:link></td>
                <td>${fieldValue(bean: userInstance, field: "mail")}</td>
                <td><g:formatBoolean boolean="${userInstance.password[0]!='!'}"/></td>
                <td><g:link mapping="namingAuthority" params="[na:params.na]" action="show"
                            id="${userInstance.id}">${message(code: 'default.button.show.label')}</g:link></td>
                <td><g:link mapping="namingAuthority" params="[na:params.na]" action="edit"
                            id="${userInstance.id}">${message(code: 'default.button.edit.label')}</g:link></td>
                <td><g:link mapping="namingAuthority" params="[na:params.na]" action="delete" id="${userInstance.id}"
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">${message(code: 'default.button.delete.label')}</g:link></td>
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
