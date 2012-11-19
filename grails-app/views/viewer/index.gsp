<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
<head>
    <title>A video display</title>
    <meta name="layout" content="${System.getProperty("layout")}">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'or.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'video-js', file: 'video-js.min.css')}"/>
    <script type="text/javascript" src="${resource(dir: 'video-js', file: 'video.min.js')}"></script>
    <script>
        _V_.options.flash.swf = "${resource(dir:'video-js', file:'video-js.swf')}";
    </script>
</head>

<body>

<div style="margin-bottom: 25px">
    <video id="av" class="video-js vjs-default-skin" controls preload="none"
           width="${file.level1.metadata.content.format.width}"
           height="${file.level1.metadata.content.format.height}"
           poster="${params.poster}"
           data-setup="{}">
        <source src="${params.source}" type='${file.level1.contentType}'/>
    </video>

</div>

<div style="margin-bottom: 25px">
    <p>Copy and past this code into your html5 webpage

    <form>
        <textarea readonly="true" style="width:800px;height: 80px;font-size: small" onclick="select()">
            <video id="example_video_1" class="video-js vjs-default-skin" controls preload="none"
                   width="${file.level1.metadata.content.format.width}" height="${file.level1.metadata.content.format.height}"
                   poster="${pid.poster}"
                   data-setup="{}">
                <source src="${pid.source}" type='${file.level1.contentType}'/>
            </video>
        </textarea>
    </form>
</div>

<div style="margin-bottom: 25px">
    <p><a href="${params.source}">Or download this ${file.level1.contentType.split('/')[0]} (format ${file.level1.contentType}; ${file.level1.length} bytes)</a>
    </p>
</div>

</body>
</html>