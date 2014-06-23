<g:set var="pidurl" value="${orfileInstance.master.metadata.resolverBaseUrl + orfileInstance.master.metadata.pid}"/>
<g:if test="${orfileInstance.master.metadata.objid}"><g:set var="metsurl"
                                                            value="${orfileInstance.master.metadata.resolverBaseUrl}${orfileInstance.master.metadata.objid}"/></g:if>


<ol class="property-list files">

    <li class="fieldcontain">
        <span id="access-label" class="property-label"><g:message code="files.access.label"
                                                                  default="Access"/></span>
        <span class="property-value" aria-labelledby="access-label">${orfileInstance.master.metadata.access}</span>
    </li>

    <g:if test="${orfileInstance.master.metadata.embargo}">
        <li class="fieldcontain">
            <span id="embargo-label" class="property-label"><g:message code="files.embargo.label"
                                                                       default="Embargo"/></span>
            <span class="property-value"
                  aria-labelledby="embargo-label">${orfileInstance.master.metadata.embargo}</span>
        </li></g:if>

    <g:if test="${orfileInstance.master.metadata.embargoAccess}">
        <li class="fieldcontain">
            <span id="embargoAccess-label" class="property-label"><g:message code="files.embargoAccess.label"
                                                                             default="Embargo access"/></span>
            <span class="property-value"
                  aria-labelledby="embargo-embargoAccess">${orfileInstance.master.metadata.embargoAccess}</span>
        </li></g:if>

    <li class="fieldcontain">
        <span id="label-label" class="property-label"><g:message code="files.label.label"
                                                                 default="Label"/></span>
        <span class="property-value" aria-labelledby="label-label">${orfileInstance.master.metadata.label}</span>
    </li>

    <li class="fieldcontain">
        <span id="pid-label" class="property-label"><g:message code="files.pid.label" default="Pid"/></span>
        <span class="property-value" aria-labelledby="pid-label">${orfileInstance.master.metadata.pid}</span>
    </li>

    <g:if test="${orfileInstance.master.metadata.lid}">
        <li class="fieldcontain">
            <span id="lid-label" class="property-label"><g:message code="files.lid.label" default="Lid"/></span>
            <span class="property-value" aria-labelledby="lid-label">${orfileInstance.master.metadata.lid}</span>
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
        <span id="metadata-label" class="property-label"><g:message code="files.metadata.label" default="Metadata"/></span>
        <span class="property-value" aria-labelledby="metadata-label">
                <g:link mapping="namingAuthority" action="show"
                        params="[na: params.na, pid: orfileInstance.master.metadata.pid, format: 'xml']">xml</g:link>
        </span>
    </li>

    <g:if test="${metsurl}">
        <li class="fieldcontain">
            <span id="objid-label" class="property-label"><g:message code="files.objid.label"
                                                                     default="Mets url"/></span>
            <span class="property-value" aria-labelledby="objid-label">
                <a href="${metsurl}"
                   target="_blank">${metsurl}</a>
            </span>
        </li>
    </g:if>

    <li class="fieldcontain">
        <span id="files-label" class="property-label"><g:message code="files.label"
                                                                 default="Files"/></span>
        <span class="property-value" aria-labelledby="files-label">
            <g:each in="${orfileInstance}" var="d">
                <g:set var="value" value="${d.value}"/>
                <table style="text-align: left">
                    <caption style="text-align: left;font-weight: bold;"><g:message
                            code="policy.access.${value.metadata.bucket}"
                            default="Bucket name"/></caption>

                    <g:if test="${value.metadata.pidType}">
                        <g:set var="pidurlqualifier" value="${pidurl + '?locatt=view:' + value.metadata.bucket}"/>
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
                                   value="${grailsApplication.config.grails.serverURL + "/file/" + d.key + "/" + orfileInstance.master.metadata.pid}"/>
                            <a href="${resolveBaseUrl}" target="_blank">${resolveBaseUrl}</a>
                        </td>
                    </tr>

                    <g:if test="${value.metadata.objid}">
                        <tr>
                            <td><g:message code="file.objid" default="Mets OBJID"/></td>
                            <td>${value.metadata.objid}</td>
                        </tr>
                        <tr>
                            <td><g:message code="file.seq" default="Order"/></td>
                            <td>${value.metadata.seq as Integer}</td>
                        </tr>
                    </g:if>

                    <tr>
                        <td><g:message code="file.contentType" default="Content type"/></td>
                        <td>${value.contentType}</td>
                    </tr>
                    <g:if test="${value.metadata.content}">
                        <tr>
                            <td><g:message code="file.metadata.dpi" default="Content"/></td>
                            <td>${value.metadata.content}</td>
                        </tr>
                    </g:if>
                    <tr>
                        <td><g:message code="file.length" default="Content length"/></td>
                        <td><g:formatNumber number="${value.length}"/></td>
                    </tr>
                    <tr>
                        <td><g:message code="file.md5" default="md5 checksum"/></td>
                        <td>${value.md5}</td>
                    </tr>
                    <tr>
                        <td><g:message code="file.timesAccessed" default="Times accessed"/></td>
                        <td><g:formatNumber number="${value.metadata.timesAccessed}"/></td>
                    </tr>
                    <tr>
                        <td><g:message code="file.firstUploadDate" default="First upload"/></td>
                        <td>${value.metadata.firstUploadDate}</td>
                    </tr>
                    <tr>
                        <td><g:message code="file.lastUploadDate" default="Last update"/></td>
                        <td>${value.metadata.lastUploadDate}</td>
                    </tr>
                    <tr>
                        <td><g:message code="file.timesUpdated" default="Times updated"/></td>
                        <td><g:formatNumber number="${value.metadata.timesUpdated}"/></td>
                    </tr>
                </table>
            </g:each>
        </span>
    </li>

</ol>
