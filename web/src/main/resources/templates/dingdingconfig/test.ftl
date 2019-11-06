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
            <label class="layui-form-label">at手机</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="text" name="atMobiles" lay-verify="required" placeholder="请填写收件地址" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">at所有人</label>
            <div class="layui-input-inline">
                <input type="checkbox" checked="checked" name="isAtAll" lay-skin="switch" lay-filter="isAtAll"
                       lay-text="是|否">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">钉钉正文</label>
            <div class="layui-input-inline" style="width:700px">
                <textarea name="content" placeholder="请填写钉钉发送内容" rows="50" style="resize: none;height: 200px"
                          autocomplete="off"
                          class="layui-input"></textarea>
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