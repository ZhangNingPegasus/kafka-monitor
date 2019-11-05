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
                <label class="layui-form-label">邮箱地址</label>
                <div class="layui-input-block">
                    <input type="text" name="host" lay-verify="required" autocomplete="off"
                           placeholder="请输入邮箱服务器地址, 例如:smtp.163.com" class="layui-input" value="${config.host!}">
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">邮箱端口</label>
                <div class="layui-input-block">
                    <input type="text" name="port" lay-verify="required" placeholder="邮箱服务器端口, 例如:25" autocomplete="off"
                           class="layui-input" value="${config.port!}">
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">用户名</label>
                <div class="layui-input-block">
                    <input type="text" name="username" lay-verify="required" autocomplete="off"
                           placeholder="请输入邮箱服务器的用户邮箱地址, 例如:xxx@163.com" class="layui-input" value="${config.username!}">
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">密码</label>
                <div class="layui-input-block">
                    <input type="password" name="password" lay-verify="required" autocomplete="off"
                           placeholder="请输入邮箱服务器用户邮箱地址对应的密码" class="layui-input" value="${config.password!}">
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