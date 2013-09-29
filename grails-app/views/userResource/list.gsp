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
    <table>
        <thead>
        <tr>
            <th>%{--Thumbnail--}%</th>
            <th>${message(code: 'userResource.pid.label', default: 'Pid')}</th>
            <th>${message(code: 'userResource.downloadLimit.label', default: 'Download Limit')}</th>
            <th>${message(code: 'userResource.downloads.label', default: 'Downloads')}</th>
            <th>${message(code: 'userResource.expirationDate.label', default: 'Expiration Date')}</th>
            <th>%{--Controls--}%</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${userResourceInstanceList}" status="i" var="userResourceInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td><g:if test="${userResourceInstance.thumbnail}">
                    <img src="${grailsApplication.config.grails.serverURL + "/file/level3/" + userResourceInstance.pid}"
                         width="100px"/>
                </g:if>
                    <g:else>
                        <g:set var="file" value="${userResourceInstance.contentType.split('/')[0] + '.png'}"/>
                        <img style="width: 100px;" src="${resource(dir: 'images/or', file: file)}"/>
                    </g:else></td>
                <td>${fieldValue(bean: userResourceInstance, field: "pid")}</td>
                <td>${userResourceInstance.downloadLimit}</td>
                <td>${userResourceInstance.downloads}</td>
                <td><g:formatDate date="${userResourceInstance.expirationDate}"/></td>
                <td><g:link class="edit" action="edit" mapping="namingAuthority"
                            params="[na: params.na, pid: userResourceInstance.pid]" id="${userInstance.id}"><g:message
                            code="default.button.edit.label" default="Edit"/></g:link>
                <g:link class="delete" action="delete" mapping="namingAuthority"
                        params="[na: params.na, pid: userResourceInstance.pid]" id="${userInstance.id}"
                        onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">${message(code: 'default.button.delete.label', default: 'Delete')}</g:link>
                </td>
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
