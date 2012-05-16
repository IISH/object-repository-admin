<%@ page import="org.objectrepository.security.Role; org.objectrepository.security.User" %>
<g:set var="entityName" value="${message(code: 'user.label', default: 'Account')}"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>

<sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CPADMIN">
    <div class="nav" role="navigation">
        <ul>
            <li><g:link class="list" action="list"><g:message code="default.list.label"
                                                              args="[entityName]"/></g:link></li>
            <li><g:link class="show" action="show" id="${userInstance.id}"><g:message code="default.show.label"
                                                                                      args="[entityName]"/></g:link></li>
            <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                                  args="[entityName]"/></g:link></li>
        </ul>
    </div>
</sec:ifAnyGranted>

<g:render template="/layouts/header" model="[instance:userInstance]"/>

<g:form method="post" onsubmit="return validate(this);">
    <g:hiddenField name="id" value="${userInstance.id}"/>
    <g:hiddenField name="version" value="${userInstance?.version}"/>
    <table>
        <tbody>

        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="role"><g:message code="user.role.label" default="Role"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
                    <g:select name="role" from="['ROLE_ADMIN','ROLE_CPADMIN','ROLE_CPUSER']"
                              value="${userInstance.firstRole}"/>
                </td>
            </tr>
        </sec:ifAnyGranted>



        <tr class="prop">
            <td valign="top" class="name">
                <label for="username"><g:message code="user.username.label" default="Username"/></label>
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

        <sec:ifAnyGranted roles="ROLE_CPADMIN">
            <g:if test="${userInstance.username != currentUsername }">
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="role"><g:message code="user.role.label" default="Role"/></label>
                    </td>
                    <td valign="top">
                        <g:select name="role" from="['ROLE_CPADMIN','ROLE_CPUSER']"
                                  value="${userInstance.firstRole}"/>
                    </td>
                </tr>
            </g:if>
        </sec:ifAnyGranted>

        <g:if test="${userInstance.username == currentUsername }">
            <g:hiddenField name="enabled" value="true"/>
        </g:if>
        <g:else>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="enabled"><g:message code="user.enabled.label" default="Enabled"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'enabled', 'errors')}">
                    <g:checkBox name="enabled" value="${userInstance?.enabled}"/>
                </td>
            </tr>
        </g:else>
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="na"><g:message code="user.na.label"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'na', 'errors')}">
                    <g:textField name="na" value="${userInstance?.na}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="o"><g:message code="user.o.label"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'o', 'errors')}">
                    <g:textField name="o" value="${userInstance?.o}"/>
                </td>
            </tr>
        </sec:ifAnyGranted>
        </tbody>
    </table>

    <div class="buttons">
        <span class="button"><g:actionSubmit class="save" action="update"
                                             value="${message(code: 'default.button.update.label', default: 'Update')}"/></span>
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CPADMIN">
            <g:if test="${userInstance?.username != currentUsername }">
                <span class="button"><g:actionSubmit class="delete" action="delete"
                                                     value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                     onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
            </g:if>
        </sec:ifAnyGranted>
    </div>
</g:form>

</body>
</html>
