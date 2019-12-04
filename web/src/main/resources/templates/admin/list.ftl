<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-card">
            <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                <div class="layui-form-item">
                    <div class="layui-inline">管理员姓名</div>
                    <div class="layui-inline" style="width:500px">
                        <input type="text" name="name" placeholder="请输入管理员姓名" autocomplete="off"
                               class="layui-input">
                    </div>
                    <div class="layui-inline">
                        <button class="layui-btn layuiadmin-btn-admin" lay-submit lay-filter="search">
                            <i class="layui-icon layui-icon-search layuiadmin-button-btn"></i>
                        </button>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>

                <script type="text/html" id="colGender">
                    {{#  if(d.gender){ }}
                    男
                    {{#  } else { }}
                    女
                    {{#  } }}
                </script>

                <script type="text/html" id="grid-toolbar">
                    <div class="layui-btn-container">
                        <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="add">新增管理员</button>
                    </div>
                </script>

                <script type="text/html" id="grid-bar">
                    <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="edit"><i
                                class="layui-icon layui-icon-edit"></i>编辑</a>
                    <a class="layui-btn layui-btn-warm layui-btn-xs" lay-event="repwd"><i
                                class="layui-icon layui-icon-password"></i>密码重置</a>
                    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del"><i
                                class="layui-icon layui-icon-delete"></i>删除</a>
                </script>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const admin = layui.admin, $ = layui.$, form = layui.form, table = layui.table;
            form.on('submit(search)', function (data) {
                const field = data.field;
                table.reload('grid', {where: field, page: 1});
            });
            table.render({
                elem: '#grid',
                url: 'list',
                toolbar: '#grid-toolbar',
                method: 'post',
                cellMinWidth: 80,
                page: true,
                limit: 15,
                limits: [15],
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'username', title: '登录名', width: 150},
                    {field: 'roleName', title: '角色名', width: 200},
                    {field: 'name', title: '姓名', width: 200},
                    {field: 'gender', title: '性别', templet: '#colGender', width: 100},
                    {field: 'phoneNumber', title: '手机号', width: 200},
                    {field: 'email', title: '邮箱地址', width: 200},
                    {field: 'remark', title: '备注'},
                    {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 235}
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'add') {
                    layer.open({
                        type: 2,
                        title: '新增管理员',
                        content: 'toadd',
                        area: ['880px', '650px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
                                if (field.password !== field.repassword) {
                                    admin.error("系统提示", "两次输入的密码不一致", function () {
                                        layero.find('iframe').contents().find('#repassword').val('');
                                        layero.find('iframe').contents().find('#password').val('').focus();
                                    });
                                    return;
                                }
                                admin.post('add', field, function () {
                                    table.reload('grid');
                                    layer.close(index);
                                }, function (result) {
                                    admin.error(admin.OPT_FAILURE, result.error);
                                    layer.close(index);
                                });
                            });
                            submit.trigger('click');
                        }
                    });
                }
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'del') {
                    layer.confirm(admin.DEL_QUESTION, function (index) {
                        admin.post("del", data, function () {
                            if (table.cache.grid.length < 2) {
                                const skip = $(".layui-laypage-skip");
                                const curPage = skip.find("input").val();
                                let page = parseInt(curPage) - 1;
                                if (page < 1) {
                                    page = 1;
                                }
                                skip.find("input").val(page);
                                $(".layui-laypage-btn").click();
                            } else {
                                table.reload('grid');
                            }
                            layer.close(index);
                        });
                    });
                } else if (obj.event === 'edit') {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-edit" style="color: #1E9FFF;"></i>&nbsp;编辑主题',
                        content: 'toedit?id=' + data.id,
                        area: ['880px', '400px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (d) {
                                const field = d.field;
                                field.id = data.id;
                                admin.post('edit', field, function () {
                                    table.reload('grid');
                                    layer.close(index);
                                }, function (result) {
                                    admin.error(admin.OPT_FAILURE, result.error);
                                });
                            });
                            submit.trigger('click');
                        }
                    });
                } else if (obj.event === 'repwd') {
                    layer.confirm("确定要重置[" + data.username + "]的密码吗?", function (index) {
                        layer.load();
                        admin.post("repwd", data, function () {
                                layer.close(index);
                                layer.closeAll('loading');
                                admin.success("系统提示", "密码重置成功, 初始密码为:admin");
                            }, function () {
                                admin.error("系统提示", "密码重置失败");
                            }
                        );
                    });
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>