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
                           placeholder="请输入邮箱服务器的用户邮箱地址, 例如:xxx@163.com" class="layui-input"
                           value="${config.username!}">
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
                    <@update>
                        <button type="button" class="layui-btn" lay-submit="" lay-filter="btnConfirm">保存</button>
                        <button type="reset" class="layui-btn layui-btn-primary">重置</button>
                        <#if (config.id)??>
                            <button id="btnTest" type="button" class="layui-btn layui-btn-normal">测试</button>
                        </#if>
                    </@update>
                </div>
            </div>
        </form>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const admin = layui.admin, form = layui.form, $ = layui.$;
            form.on('submit(btnConfirm)', function (data) {
                admin.post("save", data.field, function () {
                    admin.success("操作成功", "操作成功", function () {
                        location.reload();
                    });
                }, function (res) {
                    admin.error("操作失败", res.error);
                });
            });

            $("#btnTest").click(function () {
                layer.open({
                    type: 2,
                    title: '测试邮箱',
                    content: 'totest',
                    area: ['880px', '600px'],
                    btn: admin.BUTTONS,
                    resize: false,
                    yes: function (index, layero) {
                        const layeditCt = layer.getChildFrame('#LAY_layedit_1', index).contents().find('body');
                        const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                            submit = layero.find('iframe').contents().find('#' + submitID);
                        iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                            const field = data.field;
                            field.html = layeditCt[0].innerHTML;
                            admin.post('test', field, function () {
                                admin.success("发送成功", "邮件发送成功", function () {
                                    layer.close(index);
                                });
                            }, function (result) {
                                admin.error(admin.OPT_FAILURE, result.error);
                            });
                        });
                        submit.trigger('click');
                    }
                });
            });
        });
    </script>
    </body>
    </html>
</@compress>