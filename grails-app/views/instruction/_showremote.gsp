<g:set var="statusCode"
       value="${instructionInstance.task.name}.${instructionInstance.task.statusCode}"/>

%{--When the instruction's files are ingested, we can show progress for each plan.--}%
<div class="body" id="updateList">
    <g:if test="${ instructionInstance.ingesting }">
        <table>
            <tbody>
            <g:each in="${instructionInstance.tasks}" var="task">
                <g:if test="${task.total > 0}">
                    <g:set var="total"
                           value="${task.processed * 100 / task.total}"/>
                    <tr>
                        <td class="left"><g:message code="${task.name}.0.info"/></td>
                        <td>
                            <div class="ui-progress-bar ui-container transition">
                                <div class="ui-progress" style="width: ${total}%; ">
                                    <span class="ui-label">success: ${task.success}; failure: ${task.failure}; total: ${task.processed} / ${task.total}</span>
                                </div>
                            </div>
                            <p></p>
                        </td>
                    </tr>
                </g:if>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:elseif test="${instructionInstance.task.statusCode != 0 && instructionInstance.task.statusCode < 700}">
        <table>
            <tbody>
            <tr>
                <td colspan="2"><img class="smiley"
                                     src="${resource(dir: 'images/or', file: instructionInstance.task.statusCode + '.gif')}"
                                     alt="smiley image" title="${instructionInstance.task.name}"/>

                    <div class="ui-progress-bar ui-container transition" id="progress_bar_plan">
                        <div class="ui-progress" style="width: ${instructionInstance.task.statusCode / 8}%;">
                            <span class="ui-label"><g:message code="${statusCode}.info"/></span>
                        </div>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </g:elseif>
    <g:else><g:render template="/layouts/status" model="[statusCode:statusCode,instance:instructionInstance]"/></g:else>
</div>


