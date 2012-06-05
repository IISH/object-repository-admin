<div class="body" id="updateList">
    <table>
        <tbody>
        <g:each in="${stagingfileInstance.workflow}" var="task">
            <g:set var="statusCode"
                   value="${task.name}.${task.statusCode}"/>
            <tr>
                <td class="left"><g:message code="${task.name}.0.info"/></td>
                <g:if test="${task.is(stagingfileInstance.task.name)}">
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

</div>