<%@ page import="org.objectrepository.instruction.Instruction" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <g:set var="entityName" value="${message(code: 'instruction.label', default: 'Instruction')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="show" action="show"><g:message code="default.show.label" args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:instructionInstance]"/>

<div id="download" class="content" role="main">
    <ol class="property-list profile">
        <li class="fieldcontain">Staging area fileSet: ${instructionInstance.fileSetAlias}/instruction.xml<br/><g:link
                action="download" params="[transport:'ftp']"
                id="${instructionInstance.id}">Place the instruction on the staging area where you can use ftp to download it.</g:link><p>After selecting this option you will be taken directly back to the Instructions list.</p>
        </li>
        <li class="fieldcontain">Browser<br/><g:link mapping="namingAuthority" params="[na:params.na]" action="download" params="[transport:'http']"
                                                     id="${instructionInstance.id}">Download the instruction with your browser</g:link>
        </li>
    </ol>
</div>

</body>
</html>