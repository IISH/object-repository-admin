<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}"/>
</head>

<body>

<table style="width:400px">
    <caption>Stored master and derivative material as of now</caption>
    <thead>

    <tr>
        <th>type</th>
        <th>filecount</th>
        <th>size</th>
    </tr>
    </thead>

    <g:each in="${stats}" var="stat">
        <g:if test="${stat.bucket=='total'}"><tfoot style="font-weight: bold;"></g:if>
        <tr><td>${stat.bucket}</td><td>${stat.count}</td><td><g:formatNumber number="${stat.storageSize / 1024 / 1024}" maxFractionDigits="2"/>GB</td></tr>
        <g:if test="${stat.bucket=='total'}"></tfoot></g:if>
    </g:each>

</table>

%{--<table>
    <thead>
    <tr>
        <th>task</th>
        <th>count</th>
    </tr>
    </thead>
    <g:each in="${tasks}" var="task">
        <tr>
            <td class="left"><g:message code="${task.name}.0.info"/></td>
            <td>${total}</td>
        </tr>
    </g:each>
</table>--}%


<img src="${resource(dir: 'images', file: 'or/forthcoming.png')}"/>

<div id="badges">
    <a href="http://www.mongodb.org/" title="MongoDB"><img border="0"
                                                           title="Mongo database"
                                                           alt="MongoDB badge"
                                                           src="${resource(dir: 'images', file: 'PoweredMongoDBbeige50.png')}">
    </a>
    <a style="float:right" href="http://www.atlassian.com/" title="MongoDB"><img border="0"
                                                                                 title="Atlassian Jira, Confluence and Bamboo"
                                                                                 alt="Atlassian logo"
                                                                                 src="${resource(dir: 'images', file: 'logoAtlassianPNG.png')}">
    </a>
</div>

</body>
</html>
