<%@ page import="org.objectrepository.domain.Orfile" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
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

<g:render template="/layouts/header" model="[instance: orfileInstance]"/>

<div id="edit-files" class="content scaffold-edit" role="main">
    <g:form method="post">
        <ol class="property-list files">

            <li class="fieldcontain">
                <span id="access-label" class="property-label"><g:message code="files.access.label"
                                                                          default="Access"/></span>
                <span class="property-value" aria-labelledby="access-label">
                    <g:select from="${policyList.access}" name="access" value="${orfileInstance.metadata.access}"/>
            </li>

            <li class="fieldcontain">
                <span id="label-label" class="property-label"><g:message code="files.label.label"
                                                                         default="Label"/></span>
                <g:textField name="label" value="${orfileInstance.metadata.label}"/>
            </li>

            %{--        <g:if test="${orfileInstance.metadata.cache}">
                <li class="fieldcontain">
                    <span id="files-label" class="property-label"><g:message code="files.label"
                                                                             default="Files"/></span>
                    <span class="property-value" aria-labelledby="files-label">
                        <g:each in="${orfileInstance.metadata.cache}" var="cache">
                            <table style="text-align: left">
                                <caption style="text-align: left;font-weight: bold;"><g:message
                                                        code="policy.access.${cache.metadata.bucket}" default="${cache.metadata.bucket}"/></caption>

                                <tr>
                                    <td><g:message code="file.link" default="Location"/></td>
                                    <td><g:set var="l"><g:createLink controller="file" pid="${orfileInstance.metadata.pid}"
                                                                     action="${cache.metadata.bucket}" id="${orfileInstance.metadata.pid}"
                                                                     absolute="true"/></g:set>
                                        <a href="${l}">${l}</a>
                                    </td>
                                </tr>
                                <tr>
                                    <td><g:message code="file.contentType" default="Content type"/></td>
                                    <td>${cache.contentType}</td>
                                </tr>
                                <g:if test="${cache.metadata.content}">
                                    <tr>
                                        <td><g:message code="file.metadata.dpi" default="Content"/></td>
                                        <td>${cache.metadata.content}</td>
                                    </tr>
                                </g:if>
                                <tr>
                                    <td><g:message code="file.length" default="Content length"/></td>
                                    <td>${cache.length}</td>
                                </tr>
                                <tr>
                                    <td><g:message code="file.md5" default="md5 checksum"/></td>
                                    <td>${cache.md5}</td>
                                </tr>
                                <tr>
                                    <td><g:message code="file.timesAccessed" default="Times accessed"/></td>
                                    <td>${cache.metadata.timesAccessed}</td>
                                </tr>
                                <tr>
                                    <td><g:message code="file.firstUploadDate" default="First upload"/></td>
                                    <td>${cache.metadata.firstUploadDate}</td>
                                </tr>
                                <tr>
                                    <td><g:message code="file.lastUploadDate" default="Last update"/></td>
                                    <td>${cache.metadata.lastUploadDate}"/></td>
                                </tr>
                                <tr>
                                    <td><g:message code="file.timesUpdated" default="Times updated"/></td>
                                    <td>${cache.metadata.timesUpdated}</td>
                                </tr>
                            </table>
                        </g:each>
                    </span>
                </li>
            </g:if>--}%

        </ol>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${orfileInstance?.id}"/>
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
