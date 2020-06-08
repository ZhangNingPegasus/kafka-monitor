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
                    <div class="layui-inline">主题名称</div>
                    <div class="layui-inline" style="width:500px">
                        <input type="text" name="topicName" placeholder="请输入主题名称" autocomplete="off"
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

                <@select>
                    <script type="text/html" id="colTopicName">
                        {{#  if(d.error || d.logSize < 0){ }}
                        <a title="{{ d.error }}" href="javascript:void(0)" class="topicName layui-table-link">
                            <a href="javascript:void(0)" class="topicName layui-table-link"><span class="layui-badge">{{ d.topicName }}</span></a>
                        </a>
                        {{#  } else { }}
                        <a href="javascript:void(0)" class="topicName layui-table-link">{{ d.topicName }}</a>
                        {{#  } }}
                    </script>
                </@select>

                <script type="text/html" id="colLogSize">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.logSize }}</span>
                    {{#  } else { }}
                    {{ d.logSize }}
                    {{#  } }}
                </script>

                <script type="text/html" id="colPartitionNum">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.partitionNum }}</span>
                    {{#  } else { }}
                    {{ d.partitionNum }}
                    {{#  } }}
                </script>

                <script type="text/html" id="colPartitionIndex">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.partitionIndex }}</span>
                    {{#  } else { }}
                    {{ d.partitionIndex }}
                    {{#  } }}
                </script>

                <script type="text/html" id="colCreateTime">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.createTime }}</span>
                    {{#  } else { }}
                    {{ d.createTime }}
                    {{#  } }}
                </script>

                <script type="text/html" id="colModifyTime">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.modifyTime }}</span>
                    {{#  } else { }}
                    {{ d.modifyTime }}
                    {{#  } }}
                </script>

                <script type="text/html" id="subscribeNums">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.subscribeNums }}</span>
                    {{#  } else { }}
                    <span title="{{ d.subscribeGroupIds }}" style="cursor: pointer">{{ d.subscribeNums }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="grid-toolbar">
                    <div class="layui-btn-container">
                        <@insert>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="add">
                                <i class="layui-icon layui-icon-search layui-icon-add-1"></i>创建
                            </button>
                        </@insert>

                        <@delete>
                            <button class="layui-btn layui-btn-sm" lay-event="del">
                                <i class="layui-icon layui-icon-search layui-icon-delete"></i>删除
                            </button>
                        </@delete>
                    </div>
                </script>

                <script type="text/html" id="grid-bar">
                    <@update>
                        <a class="layui-btn layui-btn-xs" lay-event="sendMsg"><i
                                    class="layui-icon layui-icon-dialogue"></i>发送消息</a>
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
                    {type: 'checkbox', width: 50},
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'topicName', title: '主题名称', templet: '#colTopicName'},
                    {field: 'logSize', title: '${savingDays}天消息总量', templet: '#colLogSize', width: 160},
                    {field: 'subscribeNums', title: '被订阅数', templet: '#subscribeNums', width: 90},
                    {field: 'partitionNum', title: '分区数', templet: '#colPartitionNum', width: 80},
                    {field: 'partitionIndex', title: '分区索引', templet: '#colPartitionIndex', width: 200},
                    {field: 'createTime', title: '创建时间', templet: '#colCreateTime', width: 180},
                    {field: 'modifyTime', title: '修改时间', templet: '#colModifyTime', width: 180}
                    <@not_only_select>
                    , {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 240}
                    </@not_only_select>
                ]],
                done: function () {
                    $("a[class='topicName layui-table-link']").click(function () {
                        showDetail($(this).text());
                    });
                }
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'add') {
                    layer.open({
                        type: 2,
                        title: '创建主题',
                        content: 'toadd',
                        area: ['880px', '350px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
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
                } else if (obj.event === 'del') {
                    const checkData = table.checkStatus(obj.config.id).data;
                    const deleted = [];
                    $.each(checkData, function (index, item) {
                        deleted.push(item.topicName);
                    });

                    if (deleted.length < 1) {
                        admin.error('系统提醒', '当前没有选择任何的主题');
                    }

                    layer.confirm(admin.DEL_QUESTION, function (index) {
                        admin.post("del", {'topicNames': deleted.join(',')}, function () {
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
                            layer.closeAll('loading');
                        });
                    });
                }
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'del') {
                    layer.confirm(admin.DEL_QUESTION, function (index) {
                        layer.load();
                        admin.post("del", {'topicNames': data.topicName}, function () {
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
                            layer.closeAll('loading');
                        });
                    });
                } else if (obj.event === 'edit') {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-edit" style="color: #1E9FFF;"></i>&nbsp;编辑主题',
                        content: 'toedit?topicName=' + data.topicName,
                        area: ['880px', '400px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
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
                } else if (obj.event === 'sendMsg') {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-dialogue" style="color: #1E9FFF;"></i>&nbsp;发送消息, 主题名称: ' + data.topicName,
                        content: 'tosendmsg?topicName=' + data.topicName,
                        area: ['880px', '400px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
                                admin.post('sendmsg', field, function () {
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

            function showDetail(topicName) {
                layer.open({
                    type: 2,
                    title: '主题详细信息 <b>' + topicName + '</b>',
                    shadeClose: true,
                    shade: 0.8,
                    area: ['90%', '90%'],
                    content: 'todetail?topicName=' + topicName
                });
            }
        });
    </script>
    </body>
    </html>
</@compress>