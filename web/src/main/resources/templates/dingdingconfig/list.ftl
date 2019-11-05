<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">
        <form class="layui-form">
            <div class="layui-form-item">
                <label class="layui-form-label">AccessToken</label>
                <div class="layui-input-block">
                    <input type="text" name="accesstoken" lay-verify="required" autocomplete="off"
                           placeholder="请输入钉钉机器人的access token" class="layui-input" value="${config.accessToken!}">
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">Secret</label>
                <div class="layui-input-block">
                    <input type="text" name="secret" lay-verify="required" placeholder="请输入钉钉机器人的加签secret"
                           autocomplete="off"
                           class="layui-input" value="${config.secret!}">
                </div>
            </div>

            <div class="layui-form-item">
                <div class="layui-input-block">
                    <button type="button" class="layui-btn" lay-submit="" lay-filter="btnConfirm">保存</button>
                    <button type="reset" class="layui-btn layui-btn-primary">重置</button>
                </div>
            </div>
        </form>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const admin = layui.admin, form = layui.form;
            form.on('submit(btnConfirm)', function (data) {
                admin.post("save", data.field, function () {
                    admin.success("操作成功", "操作成功", function () {
                        location.reload();
                    });
                }, function (res) {
                    admin.error("操作失败", res.error);
                });
            });
        });
    </script>
    </body>
    </html>
</@compress>