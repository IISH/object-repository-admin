<%@ page import="org.springframework.security.oauth2.common.OAuth2AccessToken" %>
<g:set var="entityName" value="UserAccount"/>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
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
        <td valign="top" class="name"><g:message code="user.accessScope.label" default="Access scope"/></td>
        <td valign="top" class="value">${fieldValue(bean: userInstance, field: "accessScope")} : <g:message code="${'user.accessScope.' + userInstance.accessScope + '.label'}" /></td>
    </tr>
    <g:if test="${userInstance.accessScope == 'limited'}">
        <tr class="prop">
            <td valign="top" class="name"><g:message code="user.resource.label" default="Resource"/></td>
            <td valign="top" class="value"><g:link mapping="namingAuthority" params="[na:params.na]" controller="userResource" action="list" id="${userInstance.id}">Manage access to resources</g:link></td>
        </tr>
    </g:if>

    <tr class="prop">
        <td valign="top" class="name"><g:message code="user.enabled.label" default="Enabled"/></td>
        <td valign="top" class="value"><g:formatBoolean boolean="${userInstance.password[0] != '!'}"/></td>
    </tr>


    <g:if test="${token}">
        <tr class="prop">
            <td valign="top" class="name"><g:message code="user.key.label" default="Webservice key"/></td>
            <td valign="top" class="value">
                <table>
                    <tr>
                        <th>key</th>
                        <th>valid until</th>
                        <th></th>
                    </tr>
                    <tr>
                        <td><input id="token" type="text" size="50" value="${token.value}"/></td>
                        <td><g:formatDate date="${token.expiration}" format="yyyy-MM-dd"/></td>
                        <td><g:form mapping="namingAuthority" params="[na: params.na, id: userInstance.id]"><span
                                class="button"><g:actionSubmit
                                    class="edit" action="updatekey"
                                    value="${message(code: 'user.changekey.label', default: 'Change key')}"/></span></g:form>
                        </td>
                    </tr>
                </table>

                <p>If you know or feel your key is compromised in some way, just use the refresh option. This will generate a new key for
                you.</p>
                <hr/>

                <p>Place the key in a HTTP header request as expressed in this pseudo code:<br/>
                    HTTP-header("Authorization", "${OAuth2AccessToken.BEARER_TYPE} ${token.value}")<br/>
                    Or in your browser url: ?${OAuth2AccessToken.BEARER_TYPE_PARAMETER}=${token.value}</p>
            </td>
        </tr>
    </g:if>
    </tbody>
</table>
%{--<div id="tree" style="float:left;margin-left:50px"></div>    --}%

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
