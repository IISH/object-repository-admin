<g:each in="${policyList}" var="policy">

    <g:set var="tmp"><g:message
            code="user.dissemination.${policy}.label" default=""/></g:set>
    <g:set var="msg"> ${policy} : <g:if test="${tmp}">${tmp}</g:if><g:else><g:message
            code="user.dissemination.policy.label" args="[policy]"/></g:else></g:set>
    <g:set var="checked">${policy in userInstance.authoritiesFiltered('^ROLE_OR_DISSEMINATION_(\\d*)_(.*)$')}</g:set>

    <g:if test="${params.action in ['create', 'edit', 'index']}">
        <g:checkBox name="dissemination.${policy}"
                    checked="${checked == 'true'}"/>${msg}<br/>
    </g:if>
    <g:else>
        <g:if test="${checked == 'true'}">
            ${msg}<br/>
        </g:if>
    </g:else>

</g:each>