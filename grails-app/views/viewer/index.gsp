<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
<head>
    <title>A video display</title>
    <link rel="stylesheet" href="${resource(dir: 'video-js', file: 'video-js.min.css')}"/>
    <script type="text/javascript" src="${resource(dir: 'video-js', file: 'video.min.js')}"></script>
    <script>
        _V_.options.flash.swf = "${resource(dir:'video-js', file:'video-js.swf')}";
    </script>
</head>

<body>

<video id="example_video_1" class="video-js vjs-default-skin" controls preload="none" width="${params.width}" height="${params.height}"
       poster="${params.poster}"
       data-setup="{}">
    <source src="${params.source}" type='${params.type}'/>
</video>

<p><a href="${params.source}">${params.source}</a></p>

</body>
</html>