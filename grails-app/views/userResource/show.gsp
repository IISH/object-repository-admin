<%@ page import="org.objectrepository.security.UserResource" %>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>

<div class="nav" role="navigation">
    <ul>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:userResourceInstance]"/>
		<a href="#show-userResource" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>

		<div id="show-userResource" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list userResource">
			
				<g:if test="${userResourceInstance?.downloadLimit}">
				<li class="fieldcontain">
					<span id="downloadLimit-label" class="property-label"><g:message code="userResource.downloadLimit.label" default="Download Limit" /></span>
					
						<span class="property-value" aria-labelledby="downloadLimit-label"><g:fieldValue bean="${userResourceInstance}" field="downloadLimit"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userResourceInstance?.downloads}">
				<li class="fieldcontain">
					<span id="downloads-label" class="property-label"><g:message code="userResource.downloads.label" default="Downloads" /></span>
					
						<span class="property-value" aria-labelledby="downloads-label"><g:fieldValue bean="${userResourceInstance}" field="downloads"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userResourceInstance?.expirationDate}">
				<li class="fieldcontain">
					<span id="expirationDate-label" class="property-label"><g:message code="userResource.expirationDate.label" default="Expiration Date" /></span>
					
						<span class="property-value" aria-labelledby="expirationDate-label"><g:formatDate date="${userResourceInstance?.expirationDate}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${userResourceInstance?.pid}">
				<li class="fieldcontain">
					<span id="pid-label" class="property-label"><g:message code="userResource.pid.label" default="Pid" /></span>
					
						<span class="property-value" aria-labelledby="pid-label"><g:fieldValue bean="${userResourceInstance}" field="pid"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${userResourceInstance?.id}" />
					<g:link class="edit" action="edit" id="${userResourceInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
