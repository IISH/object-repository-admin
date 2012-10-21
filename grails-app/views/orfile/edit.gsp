<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'files.label', default: 'Files')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<a href="#edit-files" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                            default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:set var="master" value="${orfileInstanceList[0]}"/>
<g:render template="/layouts/header" model="[instance: master]"/>

<div id="edit-files" class="content scaffold-edit" role="main">
    <g:form method="post">
        <ol class="property-list files">

            <li class="fieldcontain">
                <span id="access-label" class="property-label"><g:message code="files.access.label"
                                                                          default="Access"/></span>
                <span class="property-value" aria-labelledby="access-label">
                    <g:select from="${policyList.access}" name="access" value="${master.metadata.access}"/>
            </li>

            <li class="fieldcontain">
                <span id="label-label" class="property-label"><g:message code="files.label.label"
                                                                         default="Label"/></span>
                <g:textField name="label" value="${master.metadata.label}"/>
            </li>

        </ol>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${master.metadata.pid.bytes.encodeBase64().toString()}"/>
            <g:actionSubmit class="update" action="update"
                            value="${message(code: 'default.button.update.label', default: 'Update')}"/>
            %{--<g:actionSubmit class="delete" action="delete"
                            value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>--}%
        </fieldset>
    </g:form>
</div>
</body>
</html>
