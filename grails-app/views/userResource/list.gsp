<%@ page import="org.objectrepository.security.UserResource" %>
<g:set var="entityName" value="UserResource"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="create" action="create"
                    id="${userInstance.id}"><g:message
                    code="default.new.label" args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: userResourceInstance]"/>
<div id="list-userResource" class="content scaffold-list" role="main">
    <h1><g:message code="default.list.label" args="[entityName]"/> of user: ${userInstance.username}</h1>
    <table>
        <thead>
        <tr>
            <th>Username</th>
            <g:sortableColumn property="pid" title="${message(code: 'userResource.pid.label', default: 'Pid')}"/>
            <g:sortableColumn property="downloadLimit"
                              title="${message(code: 'userResource.downloadLimit.label', default: 'Download Limit')}"/>
            <g:sortableColumn property="downloads"
                              title="${message(code: 'userResource.downloads.label', default: 'Downloads')}"/>
            <g:sortableColumn property="expirationDate"
                              title="${message(code: 'userResource.expirationDate.label', default: 'Expiration Date')}"/>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${userResourceInstanceList}" status="i" var="userResourceInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td>${fieldValue(bean: userInstance, field: "username")}</td>
                <td>${fieldValue(bean: userResourceInstance, field: "pid")}</td>
                <td>${userResourceInstance.downloadLimit / userResourceInstance.interval}</td>
                <td>${userResourceInstance.downloads / userResourceInstance.interval}</td>
                <td><g:formatDate date="${userResourceInstance.expirationDate}"/></td>
                <td>
                    <g:link class="edit" action="edit" mapping="namingAuthority"
                            params="[na: params.na, pid: userResourceInstance.pid]" id="${userInstance.id}"><g:message
                            code="default.button.edit.label" default="Edit"/></g:link>
                    <g:link class="delete" action="delete" mapping="namingAuthority"
                            params="[na: params.na, pid: userResourceInstance.pid]" id="${userInstance.id}"
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">${message(code: 'default.button.delete.label', default: 'Delete')}</g:link></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate mapping="namingAuthority" params="[na: params.na]" total="${userResourceInstanceTotal}"/>
    </div>
</div>

</body>
</html>
