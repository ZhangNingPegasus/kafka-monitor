<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
         style="padding: 20px 30px 0 0;">
        <div class="layui-form-item">
            <label class="layui-form-label">主题名称</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="text" name="topicName" lay-verify="required" placeholder="请填写主题名称" autocomplete="off"
                       readonly="readonly"
                       class="layui-input"
                       value="${topicName}">
                <span class="layui-bg-blue"><i class="layui-icon layui-icon-about"></i>&nbsp;无法修改</span>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">分区数</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="number" name="partitionNumber" lay-verify="required|number" placeholder="请填写主题的分区数量"
                       autocomplete="off"
                       autofocus="autofocus"
                       class="layui-input"
                       value="${partitionNum}">
                <span class="layui-bg-blue"><i
                            class="layui-icon layui-icon-about"></i>&nbsp;主题的主分片数量只能调大，不能调小。必须调整大于${partitionNum}个</span>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">副本分片数</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="number" name="replicationNumber" lay-verify="required|number" placeholder="请填写每个分区的副本分片数量"
                       autocomplete="off"
                       readonly="readonly"
                       class="layui-input" value="${replicasNum}">
                <span class="layui-bg-blue"><i class="layui-icon layui-icon-about"></i>&nbsp;无法修改</span>
            </div>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>

    <script>

        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form'], function () {
            parent.layer.iframeAuto(parent.layer.getFrameIndex(window.name));
        });
    </script>
    </body>
    </html>
</@compress>