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
                    <input type="text" name="topicName" placeholder="请输入主题名称" autocomplete="off" class="layui-input">
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
            <script type="text/html" id="grid-toolbar">
                <div class="layui-btn-container">
                    <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="add">创建</button>
                </div>
            </script>

            <script type="text/html" id="grid-bar">
                <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="edit"><i
                            class="layui-icon layui-icon-edit"></i>编辑</a>
                <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del"><i
                            class="layui-icon layui-icon-delete"></i>删除</a>
            </script>
        </div>
    </div>
</div>

<script>
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
        var admin = layui.admin, form = layui.form, table = layui.table;
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
                {field: 'name', title: '主题名称'},
                {field: 'logSize', title: '消息数量', width: 150},
                {field: 'partitionNum', title: '分区数', width: 150},
                {field: 'partitionIndex', title: '分区索引', width: 400},
                {field: 'createTime', title: '创建时间', width: 180},
                {field: 'modifyTime', title: '修改时间', width: 180},
                {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 150}
            ]]
        });

        table.on('toolbar(grid)', function (obj) {
            if (obj.event === 'add') {
                layer.open({
                    type: 2,
                    title: '创建主题',
                    content: 'toadd',
                    area: ['880px', '350px'],
                    btn: admin.BUTTONS,
                    resize: false,
                    yes: function (index, layero) {
                        var iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                            submit = layero.find('iframe').contents().find('#' + submitID);
                        iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                            var field = data.field;
                            admin.post('add', field, function () {
                                table.reload('grid');
                                layer.close(index);
                            }, function (result) {
                                admin.error(admin.OPT_FAILURE, result.error);
                                layer.close(index);
                            });
                        });
                        submit.trigger('click');
                    }
                });
            }
        });

        table.on('tool(grid)', function (obj) {
            var data = obj.data;
            if (obj.event === 'del') {
                layer.confirm(admin.DEL_QUESTION, function (index) {
                    admin.post("del", {topicName: data.name}, function () {
                        table.reload('grid');
                        layer.close(index);
                    });
                });
            } else if (obj.event = 'edit') {
                layer.open({
                    type: 2,
                    title: '编辑主题',
                    content: 'toedit/' + data.name,
                    area: ['880px', '400px'],
                    btn: admin.BUTTONS,
                    resize: false,
                    yes: function (index, layero) {
                        var iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                            submit = layero.find('iframe').contents().find('#' + submitID);
                        iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                            var field = data.field;
                            admin.post('edit', field, function () {
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