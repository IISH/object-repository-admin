<html>
<head>
    <meta name="layout" content="${System.getProperty("layout")}"/>
    %{--<script type="text/javascript" src="js/dojo/1.7.2/dojo/dojo.js"/>--}%
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

<g:form>
    <label for="interval">Select interval:</label>
    <g:select name="interval" value="${params.interval}" from="['year', 'month', 'week', 'day']"
              onchange="this.form.submit();"/>
</g:form>

<div class="stat">

    <table style="width:400px;white-space: nowrap;">
        <caption>Labels</caption>
        <thead>
        <tr>
            <th>date</th>
            <th>label</th>
            <th>count</th>
            <th>GB</th>
        </tr>
        </thead>
        <g:each in="${stats}" var="interval" status="i">
            <g:each in="['master', 'level1', 'level2', 'level3']" var="bucket" status="j">
                <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                    <td><g:if test="${j == 0}"><g:formatDate date="${interval._id}"
                                                             format="yyyy-MM-dd"/></g:if></td>
                    <td>${bucket}</td>
                    <td>
                        <g:formatNumber number="${interval.value['files.count.' + bucket]}" maxFractionDigits="0"/>
                    </td>
                    <td><g:if test="${interval.value['files.length.' + bucket]}">
                        <g:formatNumber number="${interval.value['files.length.' + bucket] / 1073741824}"
                                        maxFractionDigits="2"/></g:if>
                    </td>
                </tr>
            </g:each>
            <tr>
                <td></td>
                <td>Replica</td>
                <td><g:formatNumber number="${interval.value['files.count']}" maxFractionDigits="0"/></td>
                <td><g:formatNumber number="${interval.value['files.length'] / 1073741824}"
                                    maxFractionDigits="2"/></td>
            </tr>
            <tr>
                <td></td>
                <td>Total</td>
                <td><g:formatNumber number="${interval.value['files.count'] * 2}" maxFractionDigits="0"/></td>
                <td><g:formatNumber number="${interval.value['files.length'] * 2 / 1073741824}"
                                    maxFractionDigits="2"/></td>
            </tr>
        </g:each>
    </table></div>

<div class="stat">
    <table style="width:400px;white-space: nowrap;">
        <caption>Access</caption>
        <thead>
        <tr>
            <th>date</th>
            <th>type</th>
            <th>count</th>
        </tr>
        </thead>
        <g:each in="${stats}" var="interval" status="i">
            <g:set var="first" value="true"/>
            <g:each in="${interval.value}" var="item">
                <g:if test="${item.key.startsWith('access.count.')}">
                    <g:if test="${!item.value}"><g:set var="item.value" value="0"/></g:if>
                        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                            <td><g:if test="${first}"><g:formatDate date="${interval._id}"
                                                                    format="yyyy-MM-dd"/></g:if></td>
                            <td>${item.key.substring(item.key.lastIndexOf('.') + 1)}</td>
                            <td><g:formatNumber number="${item.value}" maxFractionDigits="0"/></td>
                            <g:set var="first" value="false"/>
                        </tr>
                </g:if>
            </g:each>
        </g:each>
    </table>
</div>

<div class="stat">
    <table style="width:400px;white-space: nowrap;">
        <caption>Mime type</caption>
        <thead>
        <tr>
            <th>date</th>
            <th>type</th>
            <th>count</th>
        </tr>
        </thead>
        <g:each in="${stats}" var="interval" status="i">
            <g:set var="first" value="true"/>
            <g:each in="${interval.value}" var="item">
                <g:if test="${item.key.startsWith('contentType.count.')}">
                    <g:if test="${!item.value}"><g:set var="item.value" value="0"/></g:if>
                        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                            <td><g:if test="${first}"><g:formatDate date="${interval._id}"
                                                                    format="yyyy-MM-dd"/></g:if></td>
                            <td>${item.key.substring(item.key.lastIndexOf('.') + 1)}</td>
                            <td><g:formatNumber number="${item.value}" maxFractionDigits="0"/></td>
                            <g:set var="first" value="false"/>
                        </tr>
                </g:if>
            </g:each>
        </g:each>
    </table>
</div>


%{--<img src="${resource(dir: 'images', file: 'or/forthcoming.png')}"/>--}%

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
