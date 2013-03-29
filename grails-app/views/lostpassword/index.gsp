<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="anonymous"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'login.css')}"/>
</head>

<body>

<h1>${message(code: "lostpassword.lost")}</h1>

%{--<p style="margin-bottom: 2em"><g:message code="lostpassword.index"/></p>--}%

<p>Lost your password for your administration account ?<br/>
    <a href="https://jira.socialhistoryservices.org/secure/ForgotLoginDetails.jspa">Set a new one at our Jira servicedesk.</a>
</p>

<br/><br/>

<p>Not an account holder?<br/>
    <a href="https://jira.socialhistoryservices.org/secure/Signup!default.jspa">Signup here to become a member</a>
</p>

<br/><br/>

%{--<div id='login'>
<div class='inner'>

    <g:form method="post">

        <p>
            <label><g:message code="lostpassword.mail"/></label>
            <g:textField id="mail" name="mail" value=""/>
        </p>
        <g:if test="${flash.message}">
            <p class="errors">${flash.message}</p>
        </g:if>
        <p>
            <g:actionSubmit class="send" action="newpass" value="Send"/>
        </p>

    </g:form>

</div></div>--}%

</body>

</html>



