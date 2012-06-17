<%@ page import="org.objectrepository.files.Orfile" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}">
    <g:set var="entityName" value="${message(code: 'files.label', default: 'Files')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-files" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                            default="Skip to content&hellip;"/></a>

<div id="show-files" class="content scaffold-show" role="main">
    <ol class="property-list files">

        <li class="fieldcontain">
            <span id="access-label" class="property-label"><g:message code="files.access.label"
                                                                      default="Access"/></span>
            <span class="property-value" aria-labelledby="access-label"><g:fieldValue bean="${orfileInstance}"
                                                                                      field="metadata.access"/></span>
        </li>

        <li class="fieldcontain">
            <span id="label-label" class="property-label"><g:message code="files.label.label"
                                                                     default="Label"/></span>
            <span class="property-value" aria-labelledby="label-label"><g:fieldValue bean="${orfileInstance}"
                                                                                     field="metadata.label"/></span>
        </li>

        <li class="fieldcontain">
            <span id="pid-label" class="property-label"><g:message code="files.pid.label" default="Pid"/></span>
            <span class="property-value" aria-labelledby="pid-label"><g:fieldValue bean="${orfileInstance}"
                                                                                   field="metadata.pid"/></span>
        </li>

        <li class="fieldcontain">
            <span id="resolverBaseUrl-label" class="property-label"><g:message code="files.resolverBaseUrl.label"
                                                                               default="Resolver Base Url"/></span>
            <span class="property-value" aria-labelledby="resolverBaseUrl-label">
                <g:set var="pidurl" value="${orfileInstance.metadata.resolverBaseUrl + orfileInstance.metadata.pid}"/>
                <a href="${pidurl}" target="_blank">${pidurl}</a>
            </span>
        </li>

        <g:if test="${orfileInstance.metadata.cache}">
            <li class="fieldcontain">
                <span id="files-label" class="property-label"><g:message code="files.label"
                                                                         default="Files"/></span>
                <span class="property-value" aria-labelledby="files-label">
                    <g:each in="${orfileInstance.metadata.cache}" var="cache">
                        <table style="text-align: left">
                            <caption style="text-align: left;font-weight: bold;"><g:message
                                    code="policy.access.${cache.metadata.bucket}"
                                    default="Bucket name"/></caption>

                            <g:if test="${cache.metadata.pidUrl}">
                                <tr>
                                    <td><g:message code="file.pidurl" default="Persistent url"/></td>
                                    <td>
                                        <a href="${cache.metadata.pidUrl}" target="_blank">${cache.metadata.pidUrl}</a>
                                    </td>
                                </tr>
                            </g:if>

                            <tr>
                                <td><g:message code="file.link" default="Location"/></td>
                                <td><g:set var="resolveBaseUrl"
                                           value="${grailsApplication.config.resolveBaseUrl + "/file/" + cache.metadata.bucket + "/" + cache.metadata.pid}"/>
                                    <a href="${resolveBaseUrl}" target="_blank">${resolveBaseUrl}</a>
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
                                <td><g:formatNumber number="${cache.length}"/></td>
                            </tr>
                            <tr>
                                <td><g:message code="file.md5" default="md5 checksum"/></td>
                                <td>${cache.md5}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.timesAccessed" default="Times accessed"/></td>
                                <td><g:formatNumber number="${cache.metadata.timesAccessed}"/></td>
                            </tr>
                            <tr>
                                <td><g:message code="file.firstUploadDate" default="First upload"/></td>
                                <td>${cache.metadata.firstUploadDate}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.lastUploadDate" default="Last update"/></td>
                                <td>${cache.metadata.lastUploadDate}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.timesUpdated" default="Times updated"/></td>
                                <td><g:formatNumber number="${cache.metadata.timesUpdated}"/></td>
                            </tr>
                        </table>
                    </g:each>
                </span>
            </li>
        </g:if>

    </ol>

</div>
</body>
</html>
