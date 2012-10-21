<g:set var="master" value="${orfileInstanceList[0]}"/>
<g:set var="pidurl" value="${master.metadata.resolverBaseUrl + master.metadata.pid}"/>
    <ol class="property-list files">

        <li class="fieldcontain">
            <span id="access-label" class="property-label"><g:message code="files.access.label"
                                                                      default="Access"/></span>
            <span class="property-value" aria-labelledby="access-label">${master.metadata.access}</span>
        </li>

        <li class="fieldcontain">
            <span id="label-label" class="property-label"><g:message code="files.label.label"
                                                                     default="Label"/></span>
            <span class="property-value" aria-labelledby="label-label">${master.metadata.label}</span>
        </li>

        <li class="fieldcontain">
            <span id="pid-label" class="property-label"><g:message code="files.pid.label" default="Pid"/></span>
            <span class="property-value" aria-labelledby="pid-label">${master.metadata.pid}</span>
        </li>

        <g:if test="${master.metadata.lid}">
            <li class="fieldcontain">
                <span id="lid-label" class="property-label"><g:message code="files.lid.label" default="Lid"/></span>
                <span class="property-value" aria-labelledby="lid-label">${master.metadata.lid}</span>
            </li>
        </g:if>

        <li class="fieldcontain">
            <span id="resolverBaseUrl-label" class="property-label"><g:message code="files.resolverBaseUrl.label"
                                                                               default="Base url"/></span>
            <span class="property-value" aria-labelledby="resolverBaseUrl-label">
                <a href="${pidurl}" target="_blank">${pidurl}</a>
            </span>
        </li>

            <li class="fieldcontain">
                <span id="files-label" class="property-label"><g:message code="files.label"
                                                                         default="Files"/></span>
                <span class="property-value" aria-labelledby="files-label">
                    <g:each in="${orfileInstanceList}" var="orfileInstance">
                        <table style="text-align: left">
                            <caption style="text-align: left;font-weight: bold;"><g:message
                                    code="policy.access.${orfileInstance.metadata.bucket}"
                                    default="Bucket name"/></caption>

                            <g:if test="${master.metadata.pidType}">
                                <g:set var="pidurlqualifier" value="${pidurl + '?locatt=view:' + orfileInstance.metadata.bucket}"/>
                                <tr>
                                    <td><g:message code="file.pidurl" default="Persistent url"/></td>
                                    <td>
                                        <a href="${pidurlqualifier}" target="_blank">${pidurlqualifier}</a>
                                    </td>
                                </tr>
                            </g:if>

                            <tr>
                                <td><g:message code="file.link" default="Local url"/></td>
                                <td><g:set var="resolveBaseUrl"
                                           value="${grailsApplication.config.grails.serverURL + "/file/" + orfileInstance.metadata.bucket + "/" + orfileInstance.metadata.pid}"/>
                                    <a href="${resolveBaseUrl}" target="_blank">${resolveBaseUrl}</a>
                                </td>
                            </tr>

                            <tr>
                                <td><g:message code="file.contentType" default="Content type"/></td>
                                <td>${orfileInstance.contentType}</td>
                            </tr>
                            <g:if test="${orfileInstance.metadata.content}">
                                <tr>
                                    <td><g:message code="file.metadata.dpi" default="Content"/></td>
                                    <td>${orfileInstance.metadata.content}</td>
                                </tr>
                            </g:if>
                            <tr>
                                <td><g:message code="file.length" default="Content length"/></td>
                                <td><g:formatNumber number="${orfileInstance.length}"/></td>
                            </tr>
                            <tr>
                                <td><g:message code="file.md5" default="md5 checksum"/></td>
                                <td>${orfileInstance.md5}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.timesAccessed" default="Times accessed"/></td>
                                <td><g:formatNumber number="${orfileInstance.metadata.timesAccessed}"/></td>
                            </tr>
                            <tr>
                                <td><g:message code="file.firstUploadDate" default="First upload"/></td>
                                <td>${orfileInstance.metadata.firstUploadDate}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.lastUploadDate" default="Last update"/></td>
                                <td>${orfileInstance.metadata.lastUploadDate}</td>
                            </tr>
                            <tr>
                                <td><g:message code="file.timesUpdated" default="Times updated"/></td>
                                <td><g:formatNumber number="${orfileInstance.metadata.timesUpdated}"/></td>
                            </tr>
                        </table>
                    </g:each>
                </span>
            </li>

    </ol>
