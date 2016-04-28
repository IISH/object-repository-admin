<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    <style type="text/css">
    .stat {
        float: left;
        border: 1px solid #808080;
        margin-right: 25px;
        margin-bottom: 25px;
    }
    </style>
</head>

<body>

<ul>
    <g:each in="${files}" var="file">
        <li><g:link mapping="namingAuthority" params="[na:params.na]" action="show" id="${file}">${file}</g:link></li>
    </g:each>
</ul>

<hr/>

<div id="badges" style="clear: both;">
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
