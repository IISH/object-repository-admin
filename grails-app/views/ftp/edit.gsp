<g:set var="entityName" value="${message(code: 'user.label', default: 'Account')}"/>
<!doctype html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'dynatree/skin/ui.dynatree.css')}"/>
    <g:javascript src="dynatree/jquery-ui.custom.min.js"/>
    <g:javascript src="dynatree/jquery.cookie.js"/>
    <g:javascript src="dynatree/jquery.dynatree.min.js"/>
    <g:render template="tree" model="[checkbox:true,treeOptions:[isLazy:true,hideCheckbox: true, unselectable: true]]"/>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="list" action="list"><g:message
                code="default.list.label"
                args="[entityName]"/></g:link></li>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="show" action="show"
                    id="${userInstance.id}"><g:message code="default.show.label"
                                                       args="[entityName]"/></g:link></li>
        <li><g:link mapping="namingAuthority" params="[na: params.na]" class="create" action="create"><g:message
                code="default.new.label"
                args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: userInstance]"/>

<div>
<div style="float:left">
<g:form mapping="namingAuthority" params="[na: params.na, id: userInstance.id]" method="post"
        onsubmit="return validate(this);">
    <table>
        <tbody>

        <tr class="prop">
            <td valign="top" class="name">
                <label for="username"><g:message code="user.username.label" default="username"/></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
                <g:hiddenField name="username" maxlength="30" value="${userInstance?.username}"/>
                ${userInstance?.username}
            </td>
        </tr>

        <tr class="prop">
            <td valign="top" class="name">
                <label for="password"><g:message code="user.password.label" default="Password"/></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
                <g:passwordField name="password" value="${userInstance?.password}"/>
            </td>
        </tr>

        <tr class="prop">
            <td valign="top" class="name">
                <label for="password"><g:message code="user.confirmpassword.label"
                                                 default="Confirm password"/></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'confirmpassword', 'errors')}">
                <g:passwordField name="confirmpassword" value=""/>
            </td>
        </tr>

        <tr class="prop">
            <td valign="top" class="name">
                <label for="mail"><g:message code="user.email.label" default="Email"/></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'mail', 'errors')}">
                <g:textField name="mail" value="${userInstance?.mail}"/>
            </td>
        </tr>

        <tr class="prop">
            <td valign="top" class="name">
                <label for="enabled"><g:message code="user.enabled.label" default="Enabled"/></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'enabled', 'errors')}">
                <g:checkBox name="enabled" value="${userInstance.password[0] != '!'}"/>
            </td>
        </tr>
        </tbody>
    </table>
    </div>
    <div id="tree" style="float:left;margin-left:50px"></div>
    </div>

    <div style="clear:both;"></div>

    <div class="buttons">
        <span class="button"><g:actionSubmit class="save" action="update"
                                             value="${message(code: 'default.button.update.label', default: 'Update')}"/></span>
        <span class="button"><g:actionSubmit class="delete" action="delete"
                                             value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                             onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
    </div>
</g:form>

</body>
</html>
