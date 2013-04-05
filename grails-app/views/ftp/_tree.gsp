<%@ page import="grails.converters.JSON" %>
<script type="text/javascript">
    $(function () {
        $("#tree").dynatree({
            title:"",
            selectMode:3,
            checkbox:${checkbox},
            keyPathSeparator:'$',
            fx:{ height:"toggle", duration:200 },
            autoFocus:false, // Set focus to first child, when expanding or lazy-loading.
            initAjax:{
                url:"${createLink(mapping: 'namingAuthority', params: [na:params.na], action:'homeDirectory', id:params.id)}",
                data:${treeOptions as JSON}
            },
            onLazyRead:function (node) {
                node.appendAjax({
                    url:"${createLink(mapping: 'namingAuthority', params: [na:params.na], action:'workingDirectory', id:params.id)}",
                    data:{key:node.data.key }
                });
            },
            onSelect:function (select, node) {
                $.ajax({
                    type:"POST",
                    url:"${createLink(mapping: 'namingAuthority', params: [na:params.na], action:'updateDirectory', id:params.id)}",
                    data:{select:select, key:'/' + node.data.key}
                });
            }
        });
    });
</script>