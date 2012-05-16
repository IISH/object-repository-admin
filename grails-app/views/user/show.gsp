<%@ page import="org.objectrepository.security.User" %>
<g:set var="entityName" value="${message(code: 'user.label', default: 'Account')}"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title></title>
    <script type="text/javascript">
        jQuery(document).ready(function () {
            jQuery("#token").focus(function () {
                this.select();
            })
        })
    </script>
</head>

<body>

<sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CPADMIN">
    <div class="nav" role="navigation">
        <ul>
            <li><g:link class="list" action="list"><g:message code="default.list.label"
                                                              args="[entityName]"/></g:link></li>
            <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                                  args="[entityName]"/></g:link></li>
        </ul>
    </div>
</sec:ifAnyGranted>

<g:render template="/layouts/header" model="[instance: userInstance]"/>

<table>
    <tbody>

    <tr class="prop">
        <td valign="top" class="name"><g:message code="user.id.label" default="Id"/></td>

        <td valign="top" class="value">${fieldValue(bean: userInstance, field: "id")}</td>

    </tr>

    <tr class="prop">
        <td valign="top" class="name"><g:message code="user.username.label" default="Username"/></td>

        <td valign="top" class="value">${fieldValue(bean: userInstance, field: "username")}</td>

    </tr>

    <tr class="prop">
        <td valign="top" class="name"><g:message code="user.email.label" default="Email"/></td>

        <td valign="top" class="value">${fieldValue(bean: userInstance, field: "mail")}</td>

    </tr>

    <tr class="prop">
        <td valign="top" class="name"><g:message code="user.role.label" default="Role"/></td>

        <td valign="top" class="value">${userInstance.roles}</td>

    </tr>

    <tr class="prop">
        <td valign="top" class="name"><g:message code="user.enabled.label" default="Enabled"/></td>
        <td valign="top" class="value"><g:formatBoolean boolean="${userInstance?.enabled}"/></td>
    </tr>

    <g:if test="${token}">
        <tr class="prop">
            <td valign="top" class="name"><g:message code="user.key.label" default="Webservice key"/></td>
            <td valign="top" class="value">
                <table>
                    <tr>
                        <th>key</th>
                        <th>valid until</th>
                    </tr>
                    <tr>
                        <td><input id="token" type="text" size="50" value="${token.value}"/>
                        </td>
                        <td><g:formatDate date="${token.expiration}" format="yyyy-MM-dd"/></td>
                    </tr>
                </table>

                <p>If you know or feel your key is compromised in some way, just use the refresh option. This will generate a new key for
                you.</p>
                <hr/>

                <p>Place the key in a HTTP header request as expressed in this pseude code:<br/>
                    HTTP-header("Authorization", "oauth2 ${token.value}")<br/>
                    Or in your browser url: ?oauth_token=${token.value}</p>
            </td>
        </tr>
    </g:if>

    </tbody>
</table>

<div class="buttons">
    <g:form>
        <g:hiddenField name="id" value="${userInstance?.id}"/>
        <span class="button"><g:actionSubmit class="edit" action="edit"
                                             value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
        <span class="button"><g:actionSubmit class="edit" action="updatekey"
                                             value="${message(code: 'user.changekey.label', default: 'Change key')}"/></span>
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CPADMIN">
            <g:if test="${userInstance?.username != currentUsername}">
                <span class="button"><g:actionSubmit class="delete" action="delete"
                                                     value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                     onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
            </g:if>
        </sec:ifAnyGranted>
    </g:form>
</div>

</body>
</html>
