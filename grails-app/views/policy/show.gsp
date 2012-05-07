<%@ page import="org.objectrepository.security.Policy" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'policy.label', default: 'Policy')}"/>
    <title></title>
</head>

<body>
<a href="#show-policy" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="list" action="list"><g:message code="default.list.label"
                                                          args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:policyInstance]"/>

    <div id="show-policy" class="content scaffold-show" role="main"><ol class="property-list policy">
        <li class="fieldcontain">
            <span id="access-label" class="property-label"><g:message
                    code="policy.access.label" default="Access"/></span>
            <span class="property-value" aria-labelledby="access-label"><g:fieldValue
                    bean="${policyInstance}" field="access"/></span>
        </li>

        <g:each in="${policyInstance.buckets}" var="bucket">
            <li class="fieldcontain">
                <span class="property-label"><g:message
                        code="policy.access.${bucket.bucket}" default="${bucket.bucket}"/></span>
                <span class="property-value" aria-labelledby="access-label"><g:message
                        code="policy.access.${bucket.access}" default="${bucket.access}"/></span></li>
        </g:each>

    </ol>

    <g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${policyInstance?.id}"/>
            <g:link class="edit" action="edit" id="${policyInstance?.id}"><g:message code="default.button.edit.label"
                                                                                      default="Edit"/></g:link>
            <g:actionSubmit class="delete" action="delete"
                            value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>
