<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
    <link rel="stylesheet" href="${ctx}/css/codemirror.css" media="all">
    <link rel="stylesheet" href="${ctx}/css/idea.css" media="all">
</head>
<body>

<div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin">
    <div class="layui-tab layui-tab-brief" lay-filter="docDemoTabBrief">
        <ul class="layui-tab-title">
            <li class="layui-this"><i class="layui-icon layui-icon-align-left"></i>&nbsp;&nbsp;SpringBoot配置&nbsp;&nbsp;</li>
            <li><i class="layui-icon layui-icon-align-right"></i>&nbsp;&nbsp;Spring配置&nbsp;&nbsp;</li>
            <li><i class="layui-icon layui-icon-code-circle"></i>&nbsp;&nbsp;代码配置&nbsp;&nbsp;</li>
        </ul>
        <div class="layui-tab-content" style="height: 100px;">
            <div class="layui-tab-item layui-show">
                <textarea id="txtSpringBoot" class="layui-input" autocomplete="off"
                          style="resize: none">${strSpringboot}</textarea>
            </div>
            <div class="layui-tab-item">
                <textarea id="txtSpring" class="layui-input" autocomplete="off"
                          style="resize: none">${strSpring}</textarea>
            </div>
            <div class="layui-tab-item">
                <textarea id="txtJava" class="layui-input" autocomplete="off" style="resize: none">${strJava}</textarea>
            </div>
        </div>
    </div>

    <div class="layui-form-item layui-hide">
        <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value=" 确 认 ">
    </div>
</div>

<script src="${ctx}/js/codemirror.js"></script>
<script src="${ctx}/js/autorefresh.js"></script>
<script src="${ctx}/js/active-line.js"></script>
<script src="${ctx}/js/matchbrackets.js"></script>
<script src="${ctx}/js/yaml.js"></script>
<script src="${ctx}/js/clike.js"></script>
<script src="${ctx}/js/properties.js"></script>
<script type="text/javascript">
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form'], function () {

        const txtSpringBoot = CodeMirror.fromTextArea(document.getElementById("txtSpringBoot"), {
            value: '',
            readOnly: "no",
            lineNumbers: true,
            mode: "text/x-yaml",
            matchBrackets: true,
            theme: "idea",
            styleActiveLine: true,
            autoRefresh: true
        });

        const txtSpring = CodeMirror.fromTextArea(document.getElementById("txtSpring"), {
            value: '',
            readOnly: "no",
            lineNumbers: true,
            mode: "text/x-properties",
            matchBrackets: true,
            theme: "idea",
            styleActiveLine: true,
            autoRefresh: true
        });

        const txtJava = CodeMirror.fromTextArea(document.getElementById("txtJava"), {
            value: '',
            readOnly: "no",
            lineNumbers: true,
            mode: "text/x-java",
            matchBrackets: true,
            theme: "idea",
            styleActiveLine: true,
            autoRefresh: true
        });

        txtSpringBoot.setSize('auto', '650px');
        txtSpring.setSize('auto', '650px');
        txtJava.setSize('auto', '650px');
    });
</script>
</body>
</html>