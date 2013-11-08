<%@ page import="org.objectrepository.security.UserResource" %>
<g:set var="entityName" value="UserResource"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="disseminate"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>

<div class="header" role="introduction">
    <h1>Available resources for ${userInstance.username}</h1>

    <p>Information</p>
</div>

<div id="list-userResource" class="content scaffold-list" role="main">
    <h1><g:message code="default.list.label" args="[entityName]"/> of user: ${userInstance.username}</h1>
    <table>
        <thead>
        <tr>
            <th>%{--Thumbnail--}%</th>
            <th>${message(code: 'userResource.pid.label', default: 'Pid')}</th>
            <th>${message(code: 'userResource.downloadLimit.label', default: 'Download Limit')}</th>
            <th>${message(code: 'userResource.downloads.label', default: 'Downloads')}</th>
            <th>${message(code: 'userResource.expirationDate.label', default: 'Expiration Date')}</th>
            <th>%{--Download--}%</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${userResourceInstanceList}" status="i" var="userResourceInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td>
                    <g:if test="${userResourceInstance.thumbnail}">
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
                <td><g:if test="${userResourceInstance.objid}">Use ftp</g:if><g:else>Use ftp or <a
                        href="${grailsApplication.config.grails.serverURL}/file/master/${userResourceInstance.pid}?access_token=${params.access_token}"
                        target="_blank">download it now</a></g:else></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <p>Ftp host : ${grailsApplication.config.ftp.host}<br/>
        Encryption : implicit ftp over TLS<br/>
        Logintype : normal<br/>
        Transfer mode : passive<br/>
        Username : ${userInstance.username}<br/>
        Password : ***** see your e-mail account
    </p>

</div>

</body>
</html>
