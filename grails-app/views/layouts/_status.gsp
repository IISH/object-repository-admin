<p><g:message code="${statusCode}.status"/></p>
<g:set value="${instance.countInvalidFiles}" var="countInvalidFiles"/>
<g:if test="${countInvalidFiles == 0}">
    <g:message code="${statusCode}.info"/>
</g:if>
<g:else>
    <g:link mapping="namingAuthority" style="color:red" controller="stagingfile" params="[na:params.na,orid: instance.id, filter_status:'failure']"><g:message
            code="instruction.hasNoValidFiles" args="[countInvalidFiles]"/></g:link>
</g:else>

<g:each var="task" in="${instance.workflow}">
    <g:if test="${task.info && task.statusCode > 699 && task.statusCode < 800}">
        <p style="color:red">${task.info}</p>
    </g:if>
    <g:elseif test="${task.info && task.statusCode > 799}">
        <p style="color:green;">${task.info}</p>
    </g:elseif>
</g:each>

