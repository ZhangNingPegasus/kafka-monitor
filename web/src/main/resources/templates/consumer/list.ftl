<@compress>
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
                    <div class="layui-inline">消费组名称</div>
                    <div class="layui-inline" style="width:500px">
                        <input type="text" name="groupId" placeholder="请输入消费组名称" autocomplete="off"
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
                <script type="text/html" id="grid-bar">
                    <@select>
                        <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="getDetail"><i
                                    class="layui-icon layui-icon-read"></i>查看</a>
                    </@select>
                    <@delete>
                        <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del"><i
                                    class="layui-icon layui-icon-delete"></i>删除</a>
                    </@delete>
                </script>

                <script type="text/html" id="colGroupId">
                    <a href="javascript:void(0)" class="groupid layui-table-link" data="{{ d.groupId }}">{{ d.groupId
                        }}</a>
                </script>

                <script type="text/html" id="topicCount">
                    {{#  if(d.topicCount > 0){ }}
                    <span class="layui-badge layui-bg-green">{{ d.topicCount }} ({{ d.topicNames }})</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-orange">{{ d.topicCount }} ({{ d.topicNames }})</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="activeTopicCount">
                    {{#  if(d.activeTopicCount > 0){ }}
                    <span class="layui-badge layui-bg-green">{{ d.activeTopicCount }} ({{ d.activeTopicNames }})</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-orange">{{ d.activeTopicCount }} ({{ d.activeTopicNames }})</span>
                    {{#  } }}
                </script>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const admin = layui.admin, form = layui.form, table = layui.table, $ = layui.$;
            form.on('submit(search)', function (data) {
                const field = data.field;
                table.reload('grid', {where: field, page: 1});
            });
            table.render({
                elem: '#grid',
                url: 'list',
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
                    {field: 'groupId', title: '组名称', templet: "#colGroupId"},
                    {field: 'node', title: '节点', width: 180},
                    {title: '订阅主题数', sort: true, templet: "#topicCount", width: 350},
                    {title: '活跃主题', sort: true, templet: "#activeTopicCount", width: 350},
                    {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 145}
                ]],
                done: function () {
                    $("a[class='groupid layui-table-link']").click(function () {
                        showDetail($(this).attr("data"));
                    });
                }
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'del') {
                    layer.confirm(admin.DEL_QUESTION, function (index) {
                        admin.post("del", {'consumerGroupId': data.groupId}, function () {
                            table.reload('grid');
                            layer.close(index);
                        });
                    });
                } else if (obj.event === 'getDetail') {
                    showDetail(data.groupId);
                }
            });

            function showDetail(groupId) {
                layer.open({
                    type: 2,
                    title: '消费组详细信息',
                    shadeClose: true,
                    shade: 0.8,
                    area: ['90%', '90%'],
                    content: 'todetail?groupId=' + groupId
                });
            }

        });
    </script>

    </body>
    </html>
</@compress>