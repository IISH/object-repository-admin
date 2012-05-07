<%@ page import="org.objectrepository.files.Files" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'files.label', default: 'Files')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-files" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                            default="Skip to content&hellip;"/></a>

<g:render template="/layouts/header" model="[instance:filesInstanceList]"/>

<div id="list-files" class="content scaffold-list" role="main">
    <table>
        <thead>
        <tr>

            <g:sortableColumn property="access" title="${message(code: 'files.access.label', default: 'Access')}"/>

            <g:sortableColumn property="label" title="${message(code: 'files.label.label', default: 'Label')}"/>

            <g:sortableColumn property="na" title="${message(code: 'files.na.label', default: 'Na')}"/>

            <g:sortableColumn property="pid" title="${message(code: 'files.pid.label', default: 'Pid')}"/>

        </tr>
        </thead>
        <tbody>
        <g:each in="${filesInstanceList}" status="i" var="filesInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                <td><g:link action="show"
                            id="${filesInstance.id}">${fieldValue(bean: filesInstance, field: "access")}</g:link></td>

                <td>${fieldValue(bean: filesInstance, field: "label")}</td>

                <td>${fieldValue(bean: filesInstance, field: "na")}</td>

                <td>${fieldValue(bean: filesInstance, field: "pid")}</td>

            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${filesInstanceTotal}"/>
    </div>
</div>
</body>
</html>
