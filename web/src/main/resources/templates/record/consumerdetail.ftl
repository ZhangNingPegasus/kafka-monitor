<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
         style="padding: 20px 30px 0 0;">

        <div class="layui-card-body">
            <table id="grid" lay-filter="grid"></table>
            <script type="text/html" id="colStatus">
                {{#  if(d.isConsume > 0){ }}
                <span class="layui-badge layui-bg-green">已消费</span>
                {{#  } else { }}
                <span class="layui-badge">未消费</span>
                {{#  } }}
            </script>

            <script type="text/html" id="grid-toolbar">
                <div class="layui-btn-container">
                    <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="refresh">刷新</button>
                </div>
            </script>
        </div>
    </div>
    <script type="text/javascript">
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const table = layui.table;
            table.render({
                elem: '#grid',
                url: 'listTopicConsumers?topicName=${topicName}&partitionId=${partitionId}&offset=${offset}',
                method: 'post',
                cellMinWidth: 80,
                toolbar: '#grid-toolbar',
                page: false,
                even: true,
                text: {none: '暂无相关数据'},
                cols: [[
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'groupId', title: '消费组名称'},
                    {title: '消费情况', templet: '#colStatus', width: 150}
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'refresh') {
                    table.reload('grid');
                }
            });

        });
    </script>
    </body>
    </html>
</@compress>