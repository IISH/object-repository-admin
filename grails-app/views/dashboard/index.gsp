<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}"/>
</head>

<body>

%{--<table>
    <tr>
        <th>ContentType</th>
        <th>File count</th>
        <th>File size</th>
    </tr>
    <g:each in="${contentTypes}" var="c">
        <g:if test="${c.contentType}">
        <tr>
            <td>${c.contentType}</td>
            <td>${c.total}</td>
            <td>${c.length}</td>
        </tr></g:if>
        <g:else>
            <tr>
                <td></td>
                <td></td>
                <td></td>
            </tr>
        </g:else>
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
