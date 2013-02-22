<%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
    <link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
    <link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'or.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'progressbar.css')}"/>
    %{--<dojo:header theme="soria" showSpinner="true"/>--}%
    <g:javascript library="jquery" plugin="jquery"/>
    <r:layoutResources/>
    <g:layoutHead/>
    <script type="text/javascript">
        jQuery(document).ready(function () {
            window.setInterval(function () {

                // FailSafe, in case we accidentally perpetually nestle
                var failSafe = 0;
                jQuery('form[id^=listremote]').each(function () {
                    if (++failSafe == 1) {
                        jQuery(this).submit();
                    } else {
                        document.location = document.location;
                    }
                });
            }, ${grailsApplication.config.updateList.interval});
        });
    </script>
    <script type="text/javascript">
        function validate(form) {
            var e = form.elements;

            if (!e['skippassword'] && e['confirmpassword'].value && e['password'].value != e['confirmpassword'].value) {
                alert('Your passwords do not match');
                return false;
            }
            return true;
        }
    </script>
</head>

<body>

<div id="hopeLogo"><a href="<g:resource absolute="true"/>"><h1>Shared Object Repository</h1></a>%{--<a style="float:right" href="<g:resource absolute="true"/>"><img
        src="${resource(dir: 'images', file: 'hope_logo.png')}" alt="HOPE"
        border="0"/>--}%
</div>

<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message
                code="default.home.label"/></a></li>
        <li><g:link class="home" controller="logout"><g:message
                code="logout.label"/></g:link></li>
        <li><a class="home">Logged in as: <strong><sec:username/></strong></a></li>
    </ul>
</div>

<div id="navHome">
    <ul>
        <li class="dashboard"><g:link controller="dashboard">Dashboard</g:link></li>
        <li class="users"><g:link controller="user">Accounts</g:link></li>
        <li class="profile"><g:link controller="profile">Profile</g:link></li>
        <li class="policy"><g:link controller="policy">Policies</g:link></li>
        <li class="instruction"><g:link controller="instruction">Instructions</g:link></li>
        <li class="convert"><g:link controller="orfile">Stored objects</g:link></li>
    </ul>
</div>

<div id="content"><g:layoutBody/></div>
<g:render template="/layouts/footer"/>

</body>

</html>