<div id="list-stagingfile" class="content scaffold-list">

    <table>
        <thead>
        <tr>
            <g:sortableColumn action="list" property="location"
                              title="${message(code: 'Stagingfile.location.label', default: 'Location')}"
                              params="[orid:instructionInstance.id]"/>
            <g:sortableColumn action="list" property="task.statusCode"
                              title="${message(code: 'instruction.task.statusCode.label', default: 'Status')}"
                              params="[orid:instructionInstance.id]"/>
            <g:sortableColumn action="list" property="task.statusCode"
                              title="${message(code: 'instruction.task.info.label', default: 'Info')}"
                              params="[orid:instructionInstance.id]"/>
        </tr>
        </thead>
        <tbody>
        <g:each in="${stagingfileInstanceList}" status="i" var="stagingfileInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                <td><g:link action="show"
                            id="${stagingfileInstance.id}">${fieldValue(bean: stagingfileInstance, field: "location")}</g:link></td>
                <g:render template="/layouts/task" model="[instance:stagingfileInstance]"/>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>