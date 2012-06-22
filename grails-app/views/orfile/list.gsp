<%@ page import="org.objectrepository.files.Orfile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'orfile.label', default: 'Files')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-orfile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                             default="Skip to content&hellip;"/></a>

<g:render template="/layouts/header" model="[instance: orfileInstanceList]"/>

<g:set var="filter" value="offset=${(params.offset) ?: ""}&max=${(params.max) ?: ""}&sort=${(params.sort) ?: ""}&order=${(params.order) ?: ""}"/>

<p><g:link action="download">Download metadata</g:link> |
    Show: <g:select name="label" from="${labels}" value="${params.label}"
                    onchange="document.location='?label='+this.value + '&${filter}'"/></p>

<div id="list-orfile" class="content scaffold-list" role="main">
    <table>
        <thead>
        <tr>
            <th>Type</th>
            <g:sortableColumn property="metadata.label"
                              title="${message(code: 'orfile.label.label', default: 'Label')}"/>
            <g:sortableColumn property="metadata.access"
                              title="${message(code: 'orfile.access.label', default: 'Access')}"/>
            <g:sortableColumn property="metadata.pid" title="${message(code: 'orfile.pid.label', default: 'Pid')}"/>
            <g:sortableColumn property="metadata.lastUploadDate"
                              title="${message(code: 'orfile.pid.label', default: 'Last uploaddate')}"/>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${orfileInstanceList}" status="i" var="orfileInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td><g:link action="show" id="${orfileInstance.id}">
                    <g:set var="hasPreview"
                           value="${orfileInstance.metadata.cache.find {
                               it.metadata.bucket == 'level3' && it.contentType.startsWith('image')
                           }}"/>
                    <g:if test="${hasPreview}">
                        <img src="${grailsApplication.config.grails.serverURL + "/file/level3/" + orfileInstance.metadata.pid}"
                             width="100px"/>
                    </g:if>
                    <g:else>
                        <g:set var="file" value="${orfileInstance.contentType.split('/')[0] + '.png'}"/>
                        <img style="width: 100px;" src="${resource(dir: 'images/or', file: file)}"/>
                    </g:else></g:link>
                </td>
                <td><a href="?label=${orfileInstance.metadata.label + '&' + filter}">${orfileInstance.metadata.label}</a></td>
                <td>${orfileInstance.metadata.access}</td>
                <td>${orfileInstance.metadata.pid}
                    <g:if test="${orfileInstance.metadata.lid}"><br/>${orfileInstance.metadata.lid}</g:if>
                </td>
                <td>${orfileInstance.metadata.lastUploadDate}</td>
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
