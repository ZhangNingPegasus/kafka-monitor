<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
</head>
<body>

<div class="layui-fluid">
    <div class="layui-card">

        <div class="layui-form layui-card-header layuiadmin-card-header-auto">
            <div class="layui-form-item">
                <div class="layui-inline">主题名称</div>
                <div class="layui-inline" style="width:200px">
                    <select name="topicName" lay-filter="topicName" lay-verify="required" lay-search>
                        <option value="">请选择主题</option>
                        <#list topics as topic >
                            <option value="${topic.topicName}">${topic.topicName}</option>
                        </#list>
                    </select>
                </div>

                <div class="layui-inline">分区号</div>
                <div class="layui-inline" style="width:200px">
                    <select name="partitionNum" lay-verify="required" lay-search>
                        <option value="">所有分区</option>
                    </select>
                </div>

                <div class="layui-inline">
                    <button class="layui-btn layuiadmin-btn-admin" lay-submit lay-filter="search">
                        <i class="layui-icon layui-icon-search layuiadmin-button-btn"></i>
                    </button>
                </div>
            </div>
        </div>

        <div class="layui-card-body">
            <table id="grid" lay-filter="grid"></table>
            <script type="text/html" id="grid-bar">
                <a class="layui-btn layui-btn-xs" lay-event="sendMsg"><i
                            class="layui-icon layui-icon-dialogue"></i>重发消息</a>
            </script>
        </div>
    </div>
</div>

<script>
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
        var admin = layui.admin, $ = layui.$, form = layui.form, table = layui.table;

        form.on('select(topicName)', function (data) {
            admin.post('listTopicPartitions', {'topicName': data.value}, function (res) {
                $("select[name=partitionNum]").html(" <option value=\"-1\">所有分区</option>");
                $.each(res.data, function (key, val) {
                    var option = $("<option>").val(val.partitionId).text(val.partitionId);
                    $("select[name=partitionNum]").append(option);
                    form.render('select');
                });
                $("select[name=partitionNum]").get(0).selectedIndex = 0;
            });
        });


        form.on('submit(search)', function (data) {
            var field = data.field;
            table.reload('grid', {where: field});
        });

        table.render({
            elem: '#grid',
            url: 'list',
            toolbar: '#grid-toolbar',
            method: 'post',
            cellMinWidth: 80,
            page: true,
            limit: 15,
            limits: [15],
            even: true,
            text: {
                none: '暂无相关数据'
            },
            cols: [[
                {type: 'numbers', title: '序号', width: 50},
                {field: 'topicName', title: '主题名称', width: 180},
                {field: 'partitionId', title: '分区号', width: 80},
                {field: 'offset', title: '偏移量', width: 100},
                {field: 'key', title: '消息Key', width: 150},
                {field: 'createTime', title: '创建时间', width: 180},
                {field: 'value', title: '消息体'},
                {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 105}
            ]]
        });

        table.on('tool(grid)', function (obj) {
            var data = obj.data;
            if (obj.event === 'sendMsg') {
                layer.open({
                    type: 2,
                    title: '发送消息, 主题名称: ' + data.topicName,
                    content: 'tosendmsg/' + data.topicName,
                    area: ['880px', '400px'],
                    btn: admin.BUTTONS,
                    resize: false,
                    yes: function (index, layero) {
                        var iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                            submit = layero.find('iframe').contents().find('#' + submitID);
                        iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                            var field = data.field;
                            admin.post('sendmsg', field, function () {
                                table.reload('grid');
                                layer.close(index);
                            }, function (result) {
                                admin.error(admin.OPT_FAILURE, result.error);
                            });
                        });
                        submit.trigger('click');
                    }
                });
            }
        });
    });
</script>
</body>
</html>