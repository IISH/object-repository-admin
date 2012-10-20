<!doctype html>
<html>
<head>
    <meta name="layout" content="disseminate">
    <g:set var="entityName" value="${message(code: 'files.label', default: 'Files')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-files" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                            default="Skip to content&hellip;"/></a>

<div id="show-file" class="content scaffold-show" role="main">
    <h1>File removed</h1>
    <p>Identifier: ${params.pid}</p>
    <p>Bucket: ${params.bucket}</p>
    <p>The file has been removed from the repository</p>
</div>
</body>
</html>
