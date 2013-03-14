<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="anonymous"/>
    <script type="text/javascript">
        jQuery(document).ready(function () {
            jQuery("#username").focus();
        });
    </script>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'login.css')}"/>
</head>

<body class="body">

<g:if test="${(System.getProperty('screenLogin', 'false') == 'true')}">
    <div id='login'>
        <div class='inner'>

            <g:if test='${flash.message}'>
                <div class='login_message'>${flash.message}</div>
            </g:if>

            <h1>${message(code: 'verification.signin', default: 'Please login')}</h1>

            <form action='${postUrl}' method='POST' id='loginForm' class='cssform' autocomplete='off'>

                <p>
                    <label for='username'>Login ID</label>
                    <g:if env="development"><input type='text' class='text_' name='j_username' id='username'
                                                   value='12345'/></g:if>
                    <g:else><input type='text' class='text_' name='j_username' id='username'/></g:else>
                </p>

                <p>
                    <label for='password'>Password</label><g:if env="development"><input type='password' class='text_'
                                                                                         name='j_password'
                                                                                         id='password'
                                                                                         value="12345"/></g:if>
                    <g:else><input type='password' class='text_' name='j_password' id='password'/></g:else>
                </p>

                <p>
                    <input type='submit' value='Login'/>
                </p>
            </form>

        </div>

        <p>Not an account holder?<br/><a
                href="https://jira.socialhistoryservices.org/secure/Signup!default.jspa">Signup here to become a member</a>
        </p></div>
</g:if>

</body>
</html>