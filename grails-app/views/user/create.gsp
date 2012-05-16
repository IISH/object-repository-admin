<%@ page import="org.objectrepository.security.Role; org.objectrepository.security.User" %>
<g:set var="entityName" value="${message(code: 'user.label', default: 'Account')}"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="list" action="list"><g:message code="default.list.label"
                                                          args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:userInstance]"/>

<g:form action="save" onsubmit="return validate(this);">
    <sec:ifAnyGranted roles="ROLE_CPADMIN"><g:hiddenField name="na" value="dummy"/></sec:ifAnyGranted>

    <table>
        <tbody>

        <sec:ifAnyGranted roles="ROLE_ADMIN"><tr class="prop">
            <td valign="top" class="name">
                <label for="role"><g:message code="user.role.label" default="Role"/></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
                <g:select name="role" from="['ROLE_ADMIN','ROLE_CPADMIN','ROLE_CPUSER']" value="ROLE_CPADMIN"/>
            </td>
        </tr></sec:ifAnyGranted>
        <sec:ifAnyGranted roles="ROLE_CPADMIN"><tr class="prop">
            <td valign="top" class="name">
                <label for="role"><g:message code="user.role.label" default="Role"/></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
                <g:select name="role" from="['ROLE_CPADMIN','ROLE_CPUSER']" value="ROLE_CPUSER"/>
            </td>
        </tr></sec:ifAnyGranted>

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

        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="na"><g:message code="user.na.label"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'na', 'errors')}">
                    <g:textField name="na"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="o"><g:message code="user.o.label"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'o', 'errors')}">
                    <g:textField name="o"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="skippassword"><g:message code="user.skippassword.label"
                                                         default="Do not set password"/></label>
                </td>
                <td valign="top">
                    <g:checkBox name="skippassword" value="false"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="ldap"><g:message code="user.ldap.label"
                                                         default="Add to ldap"/></label>
                </td>
                <td valign="top">
                    <g:checkBox name="ldap" value="false"/>
                </td>
            </tr>
        </sec:ifAnyGranted>
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
