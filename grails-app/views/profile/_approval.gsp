<g:if test="${instance instanceof org.objectrepository.instruction.Instruction && instance.approvalNeeded}">
    <div class="required-approval">${message(code: 'instruction.approval', default: 'Approval needed')}
        Currently approval is given by: <g:if test="${!instance.approval}">no one</g:if>
        <g:else>
            <ol>
                <g:each var="approval" in="${instance.approval}">
                    <li>${approval}</li>
                </g:each>
            </ol>
        </g:else></div>
</g:if>