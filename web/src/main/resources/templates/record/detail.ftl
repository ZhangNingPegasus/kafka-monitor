<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
</head>
<body>

<div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin" style="padding: 20px 30px 0 0;">


    <div class="layui-form-item">
        <label class="layui-form-label">主题名称</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${record.topicName}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">分区号</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${record.partitionId}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">偏移量</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${record.offset}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">消息Key</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${record.key}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">创建时间</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${record.createTime}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">消息体</label>
        <div class="layui-input-inline" style="width:700px">
            <textarea id="json" placeholder="请输入" class="layui-textarea"
                      rows="8" readonly="readonly">${record.value}</textarea>
            <button id="btnJson" type="button" class="layui-btn layui-btn-primary layui-btn-xs">json化</button>
            <button id="btnRefresh" type="button" class="layui-btn layui-btn-primary layui-btn-xs">刷新</button>
        </div>
    </div>

    <div class="layui-card-body">
        <table id="grid" lay-filter="grid"></table>

        <script type="text/html" id="colStatus">
            {{#  if(d.isConsume > 0){ }}
            <span class="layui-badge layui-bg-green">已消费</span>
            {{#  } else { }}
            <span class="layui-badge">未消费</span>
            {{#  } }}
        </script>
    </div>


</div>

<script type="text/javascript">
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
        const table = layui.table, $ = layui.$;
        table.render({
            elem: '#grid',
            url: 'listTopicConsumers?topicName=${record.topicName}&partitionId=${record.partitionId}&offset=${record.offset}',
            method: 'post',
            cellMinWidth: 80,
            page: false,
            even: true,
            text: {none: '暂无相关数据'},
            cols: [[
                {type: 'numbers', title: '序号', width: 50},
                {field: 'groupId', title: '消费组名称'},
                {title: '消费情况', templet: '#colStatus', width: 150}
            ]]
        });

        $("#btnJson").click(function () {
            $("#json").html(JSON.stringify(JSON.parse($("#json").html()), null, 4));
        });

        $("#btnRefresh").click(function () {
            table.reload('grid');
        });
    });
</script>

</body>
</html>