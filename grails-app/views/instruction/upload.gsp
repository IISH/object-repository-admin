<%@ page import="org.objectrepository.instruction.Instruction" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <g:set var="entityName" value="${message(code: 'instruction.label', default: 'Instruction')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link>
</div>

<g:render template="/layouts/header" model="[instance:instructionInstance,args:instructionInstance.fileSetAlias]"/>

<div id="upload" class="content" role="main">

    <p>
        <g:uploadForm action="upload">
            <input type="hidden" name="id" value="${instructionInstance.id}"/>
            <input type="file" name="instruction"/>
            <input type="submit" value="Submit"/>
            <input type="button" name="toggle"
                   value="${message(code: 'default.button.cancel.label', default: 'Cancel')}">
        </g:uploadForm>
    </p>

    <g:if test="${fileExists}">
        <p>Please note: there is already an instruction.xml file in the fileset:<br/>
            ${instructionInstance.fileSetAlias}/instruction.xml</p>

        <p>This file will be removed when you upload a file. If you want to use this staged instruction and not upload a new one,
        then delete the current instruction that is in the database at this moment. Deleting an instruction will not remove your files.</p>

        <p>Yes, <g:link action="delete"
                        id="${instructionInstance.id}">delete the current instruction from the database</g:link> and replace it with this processing instruction.</p>
    </g:if>

</div>

</body>
</html>