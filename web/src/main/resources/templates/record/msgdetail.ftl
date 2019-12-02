<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <script src="${ctx}/js/jquery-3.4.1.min.js"></script>
        <script src="${ctx}/js/jquery.json-editor.min.js"></script>
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
                <pre id="json"></pre>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            new JsonEditor('#json', JSON.parse('${value!''}'), {'editable': false});
        });
    </script>
    </body>
    </html>
</@compress>