<g:set var="entityName" value="${message(code: 'user.label', default: 'Account')}"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <title></title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'dynatree/skin/ui.dynatree.css')}"/>
    <g:javascript src="dynatree/jquery-ui.custom.min.js"/>
    <g:javascript src="dynatree/jquery.cookie.js"/>
    <g:javascript src="dynatree/jquery.dynatree.min.js"/>
    <g:render template="tree" model="[checkbox:false,treeOptions:[isLazy:false,hideCheckbox: true, unselectable: true]]"/>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="list" action="list"><g:message
                code="default.list.label"
                args="[entityName]"/></g:link></li>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="create" action="create"><g:message
                code="default.new.label"
                args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: userInstance]"/>

<div>
    <div style="float:left">
        <table>
            <tbody>
            <tr class="prop">
                <td valign="top" class="name"><g:message code="user.username.label" default="username"/></td>
                <td valign="top" class="value">${fieldValue(bean: userInstance, field: "username")}</td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><g:message code="user.email.label" default="Email"/></td>
                <td valign="top" class="value">${fieldValue(bean: userInstance, field: "mail")}</td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name"><g:message code="user.enabled.label" default="Enabled"/></td>
                <td valign="top" class="value"><g:formatBoolean boolean="${userInstance.password[0] != '!'}"/></td>
            </tr>
            </tbody>
        </table>
    </div>
    <div id="tree" style="float:left;margin-left:50px"></div>
</div>

<div style="clear:both;"></div>

<div class="buttons" style="margin-top: 25px;">
    <g:form mapping="namingAuthority" params="[na: params.na, id: userInstance.id]">
        <span class="button"><g:actionSubmit class="edit" action="edit"
                                             value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
        <span class="button"><g:actionSubmit class="delete" action="delete"
                                             value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                             onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
    </g:form>
</div>

</body>
</html>
