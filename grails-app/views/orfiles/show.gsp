<%@ page import="org.objectrepository.domain.Orfiles" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'files.label', default: 'Files')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-files" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                            default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance: orfilesInstance]"/>

<div id="show-files" class="content scaffold-show" role="main">
    <ol class="property-list files">

        <li class="fieldcontain">
            <span id="access-label" class="property-label"><g:message code="files.access.label"
                                                                      default="Access"/></span>
            <span class="property-value" aria-labelledby="access-label"><g:fieldValue bean="${orfilesInstance}"
                                                                                      field="metadata.access"/></span>
        </li>

        <li class="fieldcontain">
            <span id="label-label" class="property-label"><g:message code="files.label.label"
                                                                     default="Label"/></span>
            <span class="property-value" aria-labelledby="label-label"><g:fieldValue bean="${orfilesInstance}"
                                                                                     field="metadata.label"/></span>
        </li>

        <li class="fieldcontain">
            <span id="pid-label" class="property-label"><g:message code="files.pid.label" default="Pid"/></span>
            <span class="property-value" aria-labelledby="pid-label"><g:fieldValue bean="${orfilesInstance}"
                                                                                   field="metadata.pid"/></span>
        </li>

        <g:set var="pidurl" value="${orfilesInstance.metadata.resolverBaseUrl}${orfilesInstance.metadata.pid}"/>
        <li class="fieldcontain">
            <span id="resolverBaseUrl-label" class="property-label"><g:message code="files.resolverBaseUrl.label"
                                                                               default="Resolver Base Url"/></span>
            <span class="property-value" aria-labelledby="resolverBaseUrl-label">
                <a href="${pidurl}" target="_blank">${pidurl}</a>
            </span>
        </li>

        <g:if test="${orfilesInstance.metadata.files}">
            <li class="fieldcontain">
                <span id="files-label" class="property-label"><g:message code="files.label"
                                                                         default="Files"/></span>
                <span class="property-value" aria-labelledby="files-label">
                    <g:each in="${orfilesInstance.metadata.files}" var="orfile">
                        <table style="text-align: left">
                            <caption style="text-align: left;font-weight: bold;"><g:message
                                    code="policy.access.file.metadata.bucket"/></caption>

                            <tr>
                                <td><g:message code="file.link" default="Location"/></td>
                                <td><g:set var="l"><g:createLink controller="file" pid="${orfilesInstance.metadata.pid}"
                                                                 action="${orfile.bucket}" id="${orfilesInstance.metadata.pid}"
                                                                 absolute="true"/></g:set>
                                    <a href="${l}">${l}</a>
                                </td>
                            </tr>
                            <tr>
                                <td><g:message code="file.contentType" default="Content type"/></td>
                                <td>${orfile.contentType}</td>
                            </tr>
                            <g:if test="${orfile.metadata.content}">
                                <tr>
                                    <td><g:message code="file.metadata.dpi" default="Content"/></td>
                                    <td>${orfile.metadata.content}</td>
                                </tr>
                            </g:if>
                            <tr>
                                <td><g:message code="file.length" default="Content length"/></td>
                                <td>${orfile.length}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.md5" default="md5 checksum"/></td>
                                <td>${orfile.md5}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.timesAccessed" default="Times accessed"/></td>
                                <td>${orfile.metadata.timesAccessed}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.firstUploadDate" default="First upload"/></td>
                                <td>${orfile.metadata.firstUploadDate}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.lastUploadDate" default="Last update"/></td>
                                <td>${orfile.metadata.lastUploadDate}"/></td>
                            </tr>
                            <tr>
                                <td><g:message code="file.timesUpdated" default="Times updated"/></td>
                                <td>${orfile.metadata.timesUpdated}</td>
                            </tr>
                        </table>
                    </g:each>
                </span>
            </li>
        </g:if>

    </ol>
    <g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${orfilesInstance?.id}"/>
            <g:link class="edit" action="edit" id="${orfilesInstance?.id}"><g:message code="default.button.edit.label"
                                                                                      default="Edit"/></g:link>
            <g:actionSubmit class="delete" action="delete"
                            value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>
