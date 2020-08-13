<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
    <link rel="stylesheet" href="${ctx}/css/codemirror.css" media="all">
    <link rel="stylesheet" href="${ctx}/css/idea.css" media="all">
</head>
<body>

<div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
     style="padding: 20px 30px 0 0;">
    <div class="layui-form-item">
        <label class="layui-form-label">主题名称</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${topicName!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">分区号</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${partitionId!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">偏移量</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${offset!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">消息Key</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${key!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">消息时间</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${createTime!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">消息体</label>
        <div class="layui-input-inline" style="width:700px">
            <textarea id="txtJson" class="layui-input" autocomplete="off"
                      style="resize: none"><#if jsonValue??>${jsonValue}<#else>${value}</#if></textarea>
        </div>
    </div>
</div>

<script src="${ctx}/js/codemirror.js"></script>
<script src="${ctx}/js/autorefresh.js"></script>
<script src="${ctx}/js/active-line.js"></script>
<script src="${ctx}/js/matchbrackets.js"></script>
<script src="${ctx}/js/javascript.js"></script>
<script type="text/javascript">
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
        const txtJson = CodeMirror.fromTextArea(document.getElementById("txtJson"), {
            readOnly: "no",
            lineNumbers: false,
            indentUnit: 4,
            mode: "application/json",
            matchBrackets: true,
            theme: "idea",
            styleActiveLine: true,
            autoRefresh: true
        });
        txtJson.setSize('auto', '470px');
    });
</script>
</body>
</html>