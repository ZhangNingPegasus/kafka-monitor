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
            <label class="layui-form-label">收件地址</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="text" name="to" lay-verify="required" placeholder="请填写收件地址" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">邮件标题</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="text" name="subject" lay-verify="required" placeholder="请填写邮件标题" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">邮件正文</label>
            <div class="layui-input-inline" style="width:700px">
                <textarea id="html" name="html" placeholder="请填写邮件正文" autocomplete="off"
                          class="layui-input"></textarea>
            </div>
        </div>


        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form'], function () {
            layui.use('layedit', function () {
                const layedit = layui.layedit;
                const index = layedit.build('html');
                layedit.sync(index);
                parent.layer.iframeAuto(parent.layer.getFrameIndex(window.name));
            });
        });
    </script>
    </body>
    </html>
</@compress>