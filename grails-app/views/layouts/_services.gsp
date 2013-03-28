<g:each in="${instance.services}" var="service">
    <g:if test="${service.visible != false}">
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="create" controller="${service.controller}"
                    action="${service.action}"
                    id="${instance.id}">
            <g:message code="${service.name}" default="${service.name}"/>
        </g:link></li>
    </g:if>
</g:each>