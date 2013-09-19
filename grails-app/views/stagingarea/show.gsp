<g:set var="entityName" value="StagingareaAccount"/>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
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
        <td valign="top" class="name"><g:message code="user.username.label" default="Username"/></td>

        <td valign="top" class="value">${fieldValue(bean: userInstance, field: "username")}</td>

    </tr>

    <tr class="prop">
        <td valign="top" class="name"><g:message code="user.email.label" default="Email"/></td>

        <td valign="top" class="value">${fieldValue(bean: userInstance, field: "mail")}</td>

    </tr>

    <tr class="prop">
        <td valign="top" class="name"><g:message code="user.enabled.label" default="Enabled"/></td>
        <td valign="top" class="value"><g:formatBoolean boolean="${userInstance.password[0]!='!'}"/></td>
    </tr>

    </tbody>
</table>

<div class="buttons">
    <g:form mapping="namingAuthority" params="[na: params.na, id:userInstance.id]">
        <span class="button"><g:actionSubmit class="edit" action="edit"
                                             value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
        <span class="button"><g:actionSubmit class="delete" action="delete"
                                             value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                             onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
    </g:form>
</div>

</body>
</html>
