<%@ page import="org.objectrepository.instruction.Stagingfile" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <g:set var="entityName" value="${message(code: 'Stagingfile.label', default: 'Stagingfile')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav" role="navigation">
    <ul>
        <li><g:link class="show" controller="instruction" action="show" id="${instructionInstance.id}"><g:message
                code="default.show.label"
                args="['Instruction']"/></g:link></li>
        <g:render template="/layouts/services" model="[instance: instructionInstance]"/>
    </ul>
</div>

<g:render template="/layouts/header"
          model="[instance: stagingfileInstanceList, args: instructionInstance.fileSetAlias]"/>

<p>Filter on: <g:link
        action="list" params="[orid: instructionInstance.id, 'filter_status': 'failure']">failure</g:link>. <g:link
        action="list" params="[orid: instructionInstance.id]">Show all</g:link></p>

<div class="body" id="updateList">
    <g:render template="list" model="[orid: instructionInstance.id, stagingfileInstanceList: stagingfileInstanceList]"/>
</div>

<g:formRemote name="listremote" update="updateList" url="[action: 'listremote', params: [orid: instructionInstance.id,
        order: (params.order) ?: 'asc',
        sort: (params.sort) ?: 'id',
        offset: (params.offset) ?: 0,
        'filter_name': (params.filter_name) ?: null,
        'filter_status': (params.filter_status) ?: null]]"/>

<div class="pagination">
    <g:paginate total="${stagingfileInstanceTotal}" params="[orid: instructionInstance.id]"/>
</div>

</body>
</html>
