<%@ page import="org.objectrepository.security.Role; org.objectrepository.security.User" %>
<g:set var="entityName" value="${message(code: 'user.label', default: 'Account')}"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="list" action="list"><g:message code="default.list.label"
                                                          args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:userInstance]"/>

<g:form mapping="namingAuthority" params="[na:params.na]" action="save" onsubmit="return validate(this);">
    <table>
        <tbody>

        <tr class="prop">
            <td valign="top" class="name">
                <label for="username"><g:message code="user.username.label" default="Username"/></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
                <g:textField name="username" maxlength="30" value="${userInstance?.username}"/>
            </td>
        </tr>
        <tr class="prop">
            <td valign="top" class="name">
                <label for="password"><g:message code="user.password.label" default="Password"/></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
                <g:passwordField name="password" value=""/> <g:message code="user.default.autopass"/>
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
                <g:checkBox name="enabled" value="${userInstance?.enabled}"/>
            </td>
        </tr>

        <tr class="prop">
            <td valign="top" class="name">
                <label for="sendmail"><g:message code="user.sendmail.label" default="Send Notification Email"/></label>
            </td>
            <td valign="top">
                <g:checkBox name="sendmail" value="false"/>
            </td>
        </tr>
        </tbody>
    </table>

    <div class="buttons">
        <span class="button"><g:submitButton name="create" class="save" action="save"
                                             value="${message(code: 'default.button.create.label', default: 'Create')}"/></span>
    </div>
</g:form>

</body>
</html>
