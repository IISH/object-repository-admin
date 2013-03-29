<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'files.label', default: 'Files')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>

<div id="show-files" class="content scaffold-show" role="main">
    <g:render template="/layouts/orfile" model="[orfileInstance:$orfileInstance, params:$params}"/>
</div>

</body>
</html>