<div class="body" id="updateList">
    <table>
        <tbody>
        <g:each in="${stagingfileInstance.workflow}" var="task">
            <g:set var="statusCode"
                   value="${task.name}.${task.statusCode}"/>
            <tr>
                <td class="left"><g:message code="${task.name}.${task.statusCode}.info"/></td>
                <g:if test="${task.name == stagingfileInstance.task.name}">
                    <g:render template="/layouts/task" model="[instance: stagingfileInstance]"/>
                </g:if>
                <g:else>
                    <td>
                        <div class="ui-progress-bar ui-container transition">
                            <div class="ui-progress" style="width: ${task.statusCode / 8}%;">
                                <span class="ui-label"><g:message code="${statusCode}.info"/></span>
                            </div>
                        </div>
                    </td>
                </g:else>
            </tr>
        </g:each>
        </tbody>
    </table>

    <g:if test="${stagingfileInstance.failed}">
        <table>
            <tbody>
            <g:each in="${stagingfileInstance.failed}" var="task">
                <tr>
                    <td class="left">Failed: <g:message code="${task.name}.${task.statusCode}.info"/></td>
                    <td>${task.info}</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>

</div>