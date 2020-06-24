<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">

        <div class="layui-card">
            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>
                <script id="createTime" type="text/html">
                    {{#
                    var date = new Date();
                    date.setTime(d.createTime);
                    return date.format("yyyy-MM-dd HH:mm:ss");
                    }}
                </script>
                <script type="text/html" id="grid-toolbar">
                    <div class="layui-btn-container">
                        <@insert>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="add">创建</button>
                        </@insert>
                    </div>
                </script>
                <script type="text/html" id="grid-bar">
                    <@update>
                        <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="edit"><i
                                    class="layui-icon layui-icon-edit"></i>编辑</a>
                    </@update>
                    <@delete>
                        <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del"><i
                                    class="layui-icon layui-icon-delete"></i>删除</a>
                    </@delete>
                </script>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const admin = layui.admin, $ = layui.$, table = layui.table;
            tableErrorHandler();
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
                    {field: 'topicName', title: '主题名称'},
                    {field: 'fromTime', title: '开始时间(时:分:秒)', width: 150},
                    {field: 'toTime', title: '结束时间(时:分:秒)', width: 150},
                    {field: 'fromTps', title: 'TPS下限', width: 150},
                    {field: 'toTps', title: 'TPS上限', width: 150},
                    {field: 'fromMomTps', title: 'TPS变化上限', width: 150},
                    {field: 'toMomTps', title: 'TPS变化下限', width: 150},
                    {field: 'email', title: '通知邮箱', width: 150}
                    <@select>
                    , {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 140}
                    </@select>
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'add') {
                    layer.open({
                        type: 2,
                        title: '创建主题警告',
                        content: 'toadd',
                        area: ['880px', '600px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
                                if (checkField(field) === false) {
                                    return;
                                }
                                admin.post('save', field, function () {
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
                        admin.post("del", {'id': data.id}, function () {
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
                    const id = data.id;
                    layer.open({
                        type: 2,
                        title: '编辑主题警告',
                        content: 'toedit/' + id,
                        area: ['880px', '600px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
                                field.id = id;
                                if (checkField(field) === false) {
                                    return;
                                }
                                admin.post('save', field, function () {
                                    table.reload('grid');
                                    layer.close(index);
                                }, function (result) {
                                    admin.error(admin.OPT_FAILURE, result.error);
                                });
                            });
                            submit.trigger('click');
                        }
                    });
                }
            });

            function checkField(field) {
                const split = field.rangeTime.split(" - ");
                field.fromTime = split[0];
                field.toTime = split[1];
                if (field.fromTps === "" &&
                    field.toTps === "" &&
                    field.fromMomTps === "" &&
                    field.toMomTps === ""
                ) {
                    admin.error("系统提示", "TPS设置至少需要填写一个");
                    return false;
                }
                if (field.fromTps !== '' && parseInt(field.fromTps) < 0) {
                    admin.error("系统提示", "TPS下限必须大于0");
                    return false;
                }
                if (field.toTps !== '' && parseInt(field.toTps) < 0) {
                    admin.error("系统提示", "TPS上限必须大于0");
                    return false;
                }
                if (field.fromTps !== '' && field.toTps !== '' && parseInt(field.fromTps) > parseInt(field.toTps)) {
                    admin.error("系统提示", "TPS下限必须小于TPS上限");
                    return false;
                }
                if (field.fromMomTps !== '' && field.toMomTps !== '' && parseInt(field.fromMomTps) > parseInt(field.toMomTps)) {
                    admin.error("系统提示", "TPS变化下限必须小于TPS变化上限");
                    return false;
                }
                return true;
            }

        });
    </script>
    </body>
    </html>
</@compress>