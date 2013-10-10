<%@ page import="org.objectrepository.security.UserResource" %>

<div class="fieldcontain">
    <p id="username"><label for="username">Username</label>${userInstance.username}</p>
</div>

<div class="fieldcontain ${hasErrors(bean: userResourceInstance, field: 'pid', 'error')} ">
    <label for="pid">
        <g:message code="userResource.pid.label" default="Pid"/>

    </label>
    <g:textField name="pid" value="${userResourceInstance?.pid}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userResourceInstance, field: 'downloadLimit', 'error')} ">
    <label for="downloadLimit">
        <g:message code="userResource.downloadLimit.label" default="Download Limit"/>
    </label>
    <g:field type="number" name="downloadLimit" value="${userResourceInstance.downloadLimit}"/>
</div>

<div class="fieldcontain">
    <label>
        <g:message code="userResource.buckets.label"/>
    </label>
    <g:each in="${grailsApplication.config.buckets}" var="bucket">
        <g:set var="checked">${bucket in userResourceInstance.buckets}</g:set>
        <g:checkBox name="buckets.${bucket}"
                    checked="${checked == 'true'}"/>${bucket}<g:if
            test="${status != grailsApplication.config.buckets.size()}"> </g:if>
    </g:each>
</div>

<div id="t" class="fieldcontain ${hasErrors(bean: userResourceInstance, field: 'expirationDate', 'error')}">
    <label for="expirationDate"><g:checkBox name="expirationDateEnable" type="checkbox"
                                            checked="${userResourceInstance.expirationDate}"/><g:message
            code="userResource.expirationDate.label" default="Expiration Date"/></label>
    <g:datePicker disabled="true" name="expirationDate" precision="day"
                  value="${userResourceInstance?.expirationDate}"/>
</div>

<script type="text/javascript">
    $(function () {
        var expirationDateEnable = $("#expirationDateEnable");
        expirationDateEnable.click(function () {
            $("#t").find("select").attr("disabled", !this.checked);
        });
        $("#t").find("select").attr("disabled", !expirationDateEnable.checked);
    });
</script>
