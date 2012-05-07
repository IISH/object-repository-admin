<html>
<head>
    <meta name="layout" content="anonymous"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'login.css')}"/>
</head>

<body>

<div class="body">
    <h1>${message(code: "lostpassword.lost")}</h1>

    <p style="margin-bottom: 2em"><g:message code="lostpassword.index"/></p>


    <div id='login'>
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

        </div></div>

</div>
</body>

</html>



