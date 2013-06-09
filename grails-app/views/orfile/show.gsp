<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'files.label', default: 'Files')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-files" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                            default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="list" action="list"><g:message
                code="default.list.label" args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: orfileInstance]"/>

<div id="show-files" class="content scaffold-show" role="main">
    <g:render template="/layouts/orfile" model="[orfileInstance: orfileInstance]"/>
    <g:form mapping="namingAuthority"
            params="[na: params.na, id: orfileInstance.master.metadata.pid]">
        <fieldset class="buttons">
            <g:link mapping="namingAuthority" params="[na: params.na]" class="edit" action="edit"
                    id="${orfileInstance.master.metadata.pid}"><g:message
                    code="default.button.edit.label"
                    default="Edit"/></g:link>
            <g:link mapping="namingAuthority" params="[na: params.na]" class="edit" action="recreatefile"
                    id="${orfileInstance.master.metadata.pid}"><g:message
                    code="default.button.instruction.label"
                    default="New instruction"/></g:link>
        </fieldset>
    </g:form>
</div>

</body>
</html>
