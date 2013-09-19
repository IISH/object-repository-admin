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
        <li><g:link mapping="namingAuthority" params="[na:params.na]" class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
    </ul>
</div>

<g:render template="/layouts/header" model="[instance:userResourceInstance]"/>
		<a href="#list-userResource" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>

		<div id="list-userResource" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="downloadLimit" title="${message(code: 'userResource.downloadLimit.label', default: 'Download Limit')}" />
					
						<g:sortableColumn property="downloads" title="${message(code: 'userResource.downloads.label', default: 'Downloads')}" />
					
						<g:sortableColumn property="expirationDate" title="${message(code: 'userResource.expirationDate.label', default: 'Expiration Date')}" />
					
						<g:sortableColumn property="pid" title="${message(code: 'userResource.pid.label', default: 'Pid')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${userResourceInstanceList}" status="i" var="userResourceInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${userResourceInstance.id}">${fieldValue(bean: userResourceInstance, field: "downloadLimit")}</g:link></td>
					
						<td>${fieldValue(bean: userResourceInstance, field: "downloads")}</td>
					
						<td><g:formatDate date="${userResourceInstance.expirationDate}" /></td>
					
						<td>${fieldValue(bean: userResourceInstance, field: "pid")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${userResourceInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
