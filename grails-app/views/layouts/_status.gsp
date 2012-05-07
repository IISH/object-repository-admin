<p><g:message code="${statusCode}.status"/></p>
<g:set value="${instance.countInvalidFiles}" var="countInvalidFiles"/>
<g:if test="${countInvalidFiles == 0}">
    <g:message code="${statusCode}.info"/>
</g:if>
<g:else>
    <g:link style="color:red" controller="stagingfile" params="[orid: instance.id]"><g:message
            code="instruction.hasNoValidFiles" args="[countInvalidFiles]"/></g:link>
</g:else>
<g:if test="${instance.task.info}">
    <p style="color:red">${instance.task.info}</p>
</g:if>
