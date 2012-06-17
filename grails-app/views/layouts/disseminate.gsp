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
</head>

<body>

<div id="hopeLogo"></div>

<div class="nav" role="navigation"></div>

<div id="navHome"></div>

<div id="content"><g:layoutBody/></div>

<g:render template="/layouts/footer"/>

</body>

</html>