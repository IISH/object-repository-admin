<%@ page import="org.objectrepository.instruction.Profile" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'profile.label', default: 'Profile')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-profile" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>


<div class="header" role="introduction">
    <h1><g:message code="profile.show.title"/></h1>
    <p><g:message code="profile.show.intro"/></p>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
</div>

<div id="list-profile" class="content scaffold-list" role="main">

    <table>
        <thead>
        <tr>
              <th>na</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${profileInstanceList}" status="i" var="profileInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                <td><g:link action="show"
                            id="${profileInstance.id}">${fieldValue(bean: profileInstance, field: "na")}</g:link></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${profileInstanceTotal}"/>
    </div>
</div>
</body>
</html>
