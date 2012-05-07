<g:if test="${instance.task}">

    <g:set var="statusCode"
           value="${instance.task.name}.${instance.task.statusCode}"/>
    <td style="width:50%" title="${statusCode}" class="statusCode${instance.task.statusCode}">
        <g:if test="${instance.services}">
            <div class="nav service" role="service"><ul><g:render template="/layouts/services"
                                                                  model="[instance:instance]"/></ul>
            </div>
        </g:if>
        <g:else>
            <img class="smiley"
                 src="${resource(dir: 'images/or', file: instance.task.statusCode + '.gif')}"
                 alt="smiley image" title="${instance.task.name}"/>

        %{--Where the instruction\file are undergoing service node processing we present the progress here.
        The instruction will be showing the amount of files processed. The Stagingfile is a little less interesting
        --}%
            <g:if test="${instance.task.statusCode >= 500 && instance.task.statusCode < 600}">
                <g:if test="${instance.task.total && instance.task.total >= instance.task.processed}">
                    <g:set var="total" value="${instance.task.processed * 100 / instance.task.total}"/>
                    <div class="ui-progress-bar ui-container transition" id="progress_bar">
                        <div class="ui-progress" style="width: ${total}%; ">
                            <span class="ui-label">${instance.task.processed} / ${instance.task.total}</span>
                        </div>
                    </div>
                </g:if>
            </g:if>
        %{--Yet when we only view preparation phases, we present that here.--}%
            <g:elseif test="${instance.task.statusCode > 0 && instance.task.statusCode < 700}">
                <div class="ui-progress-bar ui-container transition" id="progress_bar_workflow">
                    <div class="ui-progress" style="width: ${instance.task.statusCode / 8}%;">
                        <span class="ui-label"><g:message code="${statusCode}.info"/></span>
                    </div>
                </div>
            </g:elseif>
        </g:else>
    </td>
    <td>
      <g:render template="/layouts/status" model="[statusCode:statusCode,instance:instance]"/>
    </td>
</g:if>
<g:else>
    <td colspan="2"><!-- no task --></td>
</g:else>