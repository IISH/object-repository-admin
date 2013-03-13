<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'orfile.label', default: 'Files')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
    <style type="text/css">
    .view li {
        display: block;
        float: left;
        padding-right: 25px
    }
    </style>
</head>

<body>
<a href="#list-orfile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                             default="Skip to content&hellip;"/></a>

<g:render template="/layouts/header" model="[instance: orfileInstanceList]"/>

<g:set var="filter"
       value="offset=${(params.offset) ?: ""}&max=${(params.max) ?: ""}&sort=${(params.sort) ?: ""}&order=${(params.order) ?: ""}"/>

<ul class="view">
    <li>Show: <g:select name="label" from="${labels}" value="${params.label}"
                        onchange="document.location='?label='+this.value + '&${filter}'"/></li>
    <li><g:link action="download" params="[label: params.label]">Download metadata</g:link></li>
    <li><g:link
            action="recreate" params="[label: params.label]">Recreate instruction</g:link></li>
    <li><form method="GET"><input type="submit" value="Find pid"/><input id="pid" name="pid" size="50"/></form></li>
</ul>

<div id="list-orfile" class="content scaffold-list" role="main" style="clear:both">
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
                <td><g:link action="show" id="${orfileInstance.master.metadata.pid.bytes.encodeBase64().toString()}">
                    <g:set var="hasPreview"
                           value="${orfileInstance.level3?.contentType?.startsWith('image')}"/>
                    <g:if test="${hasPreview}">
                        <img src="${grailsApplication.config.grails.serverURL + "/file/level3/" + orfileInstance.master.metadata.pid}"
                             width="100px"/>
                    </g:if>
                    <g:else>
                        <g:set var="file" value="${orfileInstance.master.contentType.split('/')[0] + '.png'}"/>
                        <img style="width: 100px;" src="${resource(dir: 'images/or', file: file)}"/>
                    </g:else></g:link>
                </td>
                <td><a href="?label=${orfileInstance.master.metadata.label + '&' + filter}">${orfileInstance.master.metadata.label}</a>
                </td>
                <td>${orfileInstance.master.metadata.access}</td>
                <td>${orfileInstance.master.metadata.pid}
                    <g:if test="${orfileInstance.master.metadata.lid}"><br/>${orfileInstance.master.metadata.lid}</g:if>
                </td>
                <td>${orfileInstance.master.metadata.lastUploadDate}</td>
                <td><g:link action="download"
                            params="[pid: orfileInstance.master.metadata.pid.bytes.encodeBase64().toString()]">metadata</g:link></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${orfileInstanceListTotal}" params="[label: params.label]"/>
    </div>
</div>
</body>
</html>
