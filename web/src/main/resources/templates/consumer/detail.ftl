<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
</head>
<body>

<div class="layui-fluid">
    <div class="layui-card">
        <div id="divGridtHeader" class="layui-card-header">消费组名称: ${groupId}</div>
        <div class="layui-card-body">
            <table id="grid" lay-filter="grid"></table>
            <script type="text/html" id="grid-bar">
                <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="getOffset"><i
                            class="layui-icon layui-icon-read"></i>查看偏移量</a>
            </script>

            <script type="text/html" id="colConsumerStatus">
                {{#  if(d.consumerStatus == 1){ }}
                <span class="layui-badge layui-bg-green">消费中</span>
                {{#  } else { }}
                <span class="layui-badge layui-bg-orange">已下线</span>
                {{#  } }}
            </script>

            <script type="text/html" id="colLag">
                {{#  if(d.consumerStatus != 1){ }}
                <span class="layui-badge">{{ d.lag }}</span>
                {{#  } else if(d.lag >= 100) { }}
                <span class="layui-badge layui-bg-orange">{{ d.lag }}</span>
                {{#  } else { }}
                <span class="layui-badge layui-bg-green">{{ d.lag }}</span>
                {{#  } }}
            </script>

            <script type="text/html" id="colLogsize">
                {{#  if(d.consumerId){ }}
                <span class="layui-badge layui-bg-green">{{ d.logSize }}</span>
                {{#  } else { }}
                <span class="layui-badge">0</span>
                {{#  } }}
            </script>

            <script type="text/html" id="colOffset">
                {{#  if(d.consumerId){ }}
                <span class="layui-badge layui-bg-green">{{ d.offset }}</span>
                {{#  } else { }}
                <span class="layui-badge">0</span>
                {{#  } }}
            </script>

        </div>
    </div>

    <div id="divGridOffset" class="layui-card" style="display: none">
        <div id="divGridOffsetHeader" class="layui-card-header"></div>
        <div class="layui-card-body">
            <table id="gridOffset" lay-filter="gridOffset"></table>
        </div>
    </div>


</div>

<script>
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
        var admin = layui.admin, table = layui.table, $ = layui.$;
        table.render({
            elem: '#grid',
            url: 'listConsumerDetails/${groupId}',
            method: 'post',
            cellMinWidth: 80,
            page: false,
            even: true,
            text: "对不起，加载出现异常！",
            cols: [[
                {type: 'numbers', title: '序号', width: 50},
                {field: 'topicName', title: '主题名称', width: 500},
                {field: "lag", title: '堆积消息数量', templet: '#colLag', width: 200},
                {title: '消费状态', templet: '#colConsumerStatus', width: 300},
                {fixed: 'right', title: '操作', toolbar: '#grid-bar'}
            ]]
        });

        table.on('tool(grid)', function (obj) {
            var data = obj.data;
            if (obj.event === 'getOffset') {
                table.render({
                    elem: '#gridOffset',
                    url: 'listOffsetInfo/${groupId}/' + data.topicName,
                    method: 'post',
                    cellMinWidth: 80,
                    page: false,
                    even: true,
                    text: "对不起，加载出现异常！",
                    cols: [[
                        {field: 'partitionId', title: '分区号', width: 100},
                        {field: 'logSize', templet: "#colLogsize", title: '消息数量', width: 200},
                        {field: 'offset', templet: "#colOffset", title: '已消费数量', width: 200},
                        {field: 'lag', title: '未消费数量', width: 200},
                        {field: 'consumerId', title: '消费者ID'}
                    ]],
                    done: function (res) {
                        var logsize = 0, offset = 0, lag = 0;
                        var cls = "";
                        for (var i = 0; i < res.data.length; i++) {
                            if (res.data[i].logSize) logsize += res.data[i].logSize;
                            if (res.data[i].offset) offset += res.data[i].offset;
                            if (res.data[i].lag) lag += res.data[i].lag;
                            if (res.data[i].consumerId == "") {
                                cls = "";
                            } else if (lag >= 100) {
                                cls = "layui-bg-orange";
                            } else {
                                cls = "layui-bg-green";
                            }
                        }
                        $("#divGridOffsetHeader").html("主题：" + data.topicName + ", 分区数:" + res.data.length + " - 总共消息" + logsize + "条, 已消费" + offset + "条。" + "  当前有<span class='layui-badge " + cls + "'><b><i>" + lag + "</i></b></span>条信息堆积");
                        $("td[data-field=topicName]").each(function (a, td) {
                            if ($(td).find("div").html() === data.topicName) {
                                $(td).siblings("td[data-field=lag]").find("div > span").attr("class", "layui-badge " + cls);
                                $(td).siblings("td[data-field=lag]").find("div > span").html(lag);
                            }

                        });

                    }
                });
                $("#divGridOffset").show();
            }
        });
    });
</script>

</body>
</html>