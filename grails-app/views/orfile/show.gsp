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
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: orfileInstanceList[0]]"/>

<div id="show-files" class="content scaffold-show" role="main">
    <g:render template="/layouts/orfile" model="${orfileInstanceList}"/>
    <g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${orfileInstanceList[0].metadata.pid.bytes.encodeBase64().toString()}"/>
            <g:link class="edit" action="edit" id="${orfileInstanceList[0].metadata.pid.bytes.encodeBase64().toString()}"><g:message
                    code="default.button.edit.label"
                    default="Edit"/></g:link>
        </fieldset>
    </g:form>
</div>

</body>
</html>
