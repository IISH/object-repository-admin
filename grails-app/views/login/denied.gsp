<html>
<head>
    <meta name="layout" content="authenticated"/>
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><g:link class="logout" controller="logout"><g:message code="logout.label"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="user.default.unauthorized" /></h1>
    <p><g:message code="user.default.forthcoming"/></p>
</div>

</body>

</html>