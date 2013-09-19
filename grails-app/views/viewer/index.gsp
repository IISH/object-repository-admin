<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js" xmlns="http://www.w3.org/1999/html"><!--<![endif]-->
<head>
    <title>A video display</title>
    <meta name="layout" content="${System.getProperty("layout")}">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'or.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'video-js', file: 'video-js.min.css')}"/>
    <script type="text/javascript" src="${resource(dir: 'video-js', file: 'video.js')}"></script>
    <script>
        _V_.options.flash.swf = "${resource(dir:'video-js', file:'video-js.swf')}";
    </script>
</head>

<body>

<div style="margin-bottom: 25px">
    <video id="av" class="video-js vjs-default-skin" controls preload="none"
           width="${file.level1.metadata.content.streams.width}"
           height="${file.level1.metadata.content.streams.height}"
           poster="${params.poster}"
           data-setup="{}">
        <source src="${params.source}" type='${file.level1.contentType}'/>
    </video>

</div>

<div style="margin-bottom: 25px">
    <form>
        <p>To place ${file.level1.contentType.split('/')[0]} in your webpage, place in the &lt;head&gt;<br/>
            <textarea readonly="true" style="width:800px;height: 80px;font-size: small" onclick="select()">
                <link href="http://vjs.zencdn.net/c/video-js.css" rel="stylesheet">
                <script src="http://vjs.zencdn.net/c/video.js"></script>
            </textarea></p>

        <p>and add this in your &lt;body&gt;:<br/>
            <textarea readonly="true" style="width:800px;font-size: small" onclick="select()">
                <video id="example_video_1" class="video-js vjs-default-skin" controls preload="none"
                       width="${file.level1.metadata.content.streams.width}"
                       height="${file.level1.metadata.content.streams.height}"
                       poster="${pid.poster}"
                       data-setup="{}">
                    <source src="${pid.source}" type='${file.level1.contentType}'/>
                </video>
            </textarea></p>
    </form>

</div>

<div style="margin-bottom: 25px">
    <p><a href="${params.source}">Or download this ${file.level1.contentType.split('/')[0]} (format ${file.level1.contentType}; ${file.level1.length} bytes)</a>
    </p>
</div>

</body>
</html>