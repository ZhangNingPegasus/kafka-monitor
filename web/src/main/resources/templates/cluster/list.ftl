<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
</head>
<body>

<div class="layui-fluid">
    <div class="layui-card">
        <div class="layui-card-body">
            <table id="grid" lay-filter="grid"></table>
        </div>
    </div>
</div>

<script>
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
        var admin = layui.admin, form = layui.form, table = layui.table;
        table.render({
            elem: '#grid',
            url: 'list',
            toolbar: '#grid-toolbar',
            method: 'post',
            cellMinWidth: 80,
            page: false,
            even: true,
            text: {
                none: '暂无相关数据'
            },
            cols: [[
                {field: 'name', title: '编号', width: 200},
                {field: 'host', title: '地址'},
                {field: 'port', title: '端口', width: 100},
                {field: 'jmxPort', title: 'JMX端口', width: 100},
                {field: 'createTime', title: '创建时间', width: 200},
                {field: 'version', title: '版本', width: 100}
            ]]
        });
    });
</script>


</body>
</html>