<div id="list-instruction" class="content scaffold-list" role="main">
    <table>
        <thead>
        <tr>
            <g:sortableColumn action="list" property="fileSetAlias"
                              title="${message(code: 'instruction.fileSetAlias.label', default: 'Orfile Set')}"/>
            <g:sortableColumn action="list" property="label"
                              title="${message(code: 'instruction.label.label', default: 'Label')}"/>
            <g:sortableColumn action="list" property="task.statusCode"
                              title="${message(code: 'instruction.task.statusCode.label', default: 'Status')}"/>
            <g:sortableColumn action="list" property="task.statusCode"
                              title="${message(code: 'instruction.task.info.label', default: 'Info')}"/>
        </tr>
        </thead>
        <tbody id="list">
        <g:each in="${instructionInstanceList}" status="i" var="instructionInstance">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" id="ud${instructionInstance.id}">
                <td>
                    <g:link action="show"
                            id="${instructionInstance.id}">${fieldValue(bean: instructionInstance, field: "fileSetAlias")}</g:link>
                <br/>
                    ${fieldValue(bean: instructionInstance, field: "declaredFiles")} declared files.
                </td>
                <td>${fieldValue(bean: instructionInstance, field: "label")}</td>
                <g:render template="/layouts/task" model="[instance: instructionInstance]"/>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
