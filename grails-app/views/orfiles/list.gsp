<%@ page import="org.objectrepository.domain.Orfiles" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'orfiles.label', default: 'Files')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-orfiles" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<g:render template="/layouts/header" model="[instance: orfilesInstanceList]"/>

<div id="list-orfiles" class="content scaffold-list" role="main">
    <table>
        <thead>
        <tr>
            <g:sortableColumn property="metadata.access"
                              title="${message(code: 'orfiles.access.label', default: 'Access')}"/>
            <g:sortableColumn property="metadata.label"
                              title="${message(code: 'orfiles.label.label', default: 'Label')}"/>
            <g:sortableColumn property="metadata.pid" title="${message(code: 'orfiles.pid.label', default: 'Pid')}"/>
            <g:sortableColumn property="metadata.lastUploadDate"
                              title="${message(code: 'orfiles.pid.label', default: 'Last uploaddate')}"/>
        </tr>
        </thead>
        <tbody>
        <g:each in="${orfilesInstanceList}" status="i" var="orfilesInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                <td><g:link action="show"
                            id="${orfilesInstance.id}">${fieldValue(bean: orfilesInstance.metadata, field: "access")}</g:link></td>
                <td>${fieldValue(bean: orfilesInstance.metadata, field: "label")}</td>
                <td>${fieldValue(bean: orfilesInstance.metadata, field: "pid")}</td>
                <td>${fieldValue(bean: orfilesInstance.metadata, field: "lastUploadDate")}</td>

            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${orfilesInstanceListTotal}"/>
    </div>
</div>
</body>
</html>
