<%@ page import="org.objectrepository.instruction.Instruction" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <g:set var="entityName" value="${message(code: 'instruction.label', default: 'Instruction')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
    <style type="text/css">
    .service li {
        display: list-item;
    }
    </style>
</head>

<body>

<g:render template="/layouts/header" model="[instance: instructionInstanceList]"/>

<div class="body" id="updateList">
    <g:render template="list" model="[instructionInstanceList, instructionInstanceTotal]"/>
</div>

<g:formRemote name="listremote" update="updateList" url="[action: 'listremote', params:params]" />

<div class="pagination">
    <g:paginate action="list" total="${instructionInstanceTotal}"/>
</div>
</body>
</html>
