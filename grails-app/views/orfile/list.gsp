<%@ page import="org.objectrepository.domain.Orfile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'orfile.label', default: 'Files')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-orfile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<g:render template="/layouts/header" model="[instance: orfileInstanceList]"/>

<p><g:link action="download">Download metadata</g:link></p>

<div id="list-orfile" class="content scaffold-list" role="main">
    <table>
        <thead>
        <tr>
            <g:sortableColumn property="metadata.access"
                              title="${message(code: 'orfile.access.label', default: 'Access')}"/>
            <g:sortableColumn property="metadata.label"
                              title="${message(code: 'orfile.label.label', default: 'Label')}"/>
            <g:sortableColumn property="metadata.pid" title="${message(code: 'orfile.pid.label', default: 'Pid')}"/>
            <g:sortableColumn property="metadata.lastUploadDate"
                              title="${message(code: 'orfile.pid.label', default: 'Last uploaddate')}"/>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${orfileInstanceList}" status="i" var="orfileInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                <td><g:link action="show"
                            id="${orfileInstance.id}">${fieldValue(bean: orfileInstance.metadata, field: "access")}</g:link></td>
                <td>${fieldValue(bean: orfileInstance.metadata, field: "label")}</td>
                <td>${fieldValue(bean: orfileInstance.metadata, field: "pid")}</td>
                <td>${fieldValue(bean: orfileInstance.metadata, field: "lastUploadDate")}</td>
                <td><g:link action="download" id="${orfileInstance.id}">metadata</g:link></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${orfileInstanceListTotal}"/>
    </div>
</div>
</body>
</html>
