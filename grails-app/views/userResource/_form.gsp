<%@ page import="org.objectrepository.security.UserResource" %>



<div class="fieldcontain ${hasErrors(bean: userResourceInstance, field: 'downloadLimit', 'error')} ">
	<label for="downloadLimit">
		<g:message code="userResource.downloadLimit.label" default="Download Limit" />
		
	</label>
	<g:field type="number" name="downloadLimit" value="${userResourceInstance.downloadLimit}" />
</div>

<div class="fieldcontain ${hasErrors(bean: userResourceInstance, field: 'downloads', 'error')} ">
	<label for="downloads">
		<g:message code="userResource.downloads.label" default="Downloads" />
		
	</label>
	<g:field type="number" name="downloads" value="${userResourceInstance.downloads}" />
</div>

<div class="fieldcontain ${hasErrors(bean: userResourceInstance, field: 'expirationDate', 'error')} ">
	<label for="expirationDate">
		<g:message code="userResource.expirationDate.label" default="Expiration Date" />
		
	</label>
	<g:datePicker name="expirationDate" precision="day" value="${userResourceInstance?.expirationDate}" />
</div>

<div class="fieldcontain ${hasErrors(bean: userResourceInstance, field: 'pid', 'error')} ">
	<label for="pid">
		<g:message code="userResource.pid.label" default="Pid" />
		
	</label>
	<g:textField name="pid" value="${userResourceInstance?.pid}" />
</div>

