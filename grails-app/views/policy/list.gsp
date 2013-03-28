<%@ page import="org.objectrepository.security.Policy" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'policy.label', default: 'Policy')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-policy" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:policyInstance]"/>

<div id="list-policy" class="content scaffold-list" role="main">
    <table>
        <thead>
        <tr>

            <g:sortableColumn property="access"
                              title="${message(code: 'policy.access.label', default: 'Access')}"/>
            <th><g:message code="access.buckets" default="Master and derivatives"/></th>

        </tr>
        </thead>
        <tbody>
        <g:each in="${policyInstanceList}" status="i" var="policyInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td><g:link mapping="namingAuthority" params="[na:params.na]" action="show"
                            id="${policyInstance.id}">${fieldValue(bean: policyInstance, field: "access")}</g:link>
                </td>

                <td><ul><g:each in="${policyInstance.buckets}" var="bucket">
                    <li><g:message
                        code="policy.access.${bucket.bucket}" default="${bucket.bucket}"/>: ${bucket.access}</li>
                </g:each></ul>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate mapping="namingAuthority" params="[na:params.na]" total="${policyInstanceTotal}"/>
    </div>
</div>
</body>
</html>
