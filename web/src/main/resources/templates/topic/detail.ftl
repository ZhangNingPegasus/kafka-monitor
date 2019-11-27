<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-card">
            <div id="divGridtHeader" class="layui-card-header"></div>
            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>
            </div>
        </div>

        <div class="layui-card">
            <div class="layui-card-header">主题其他信息</div>
            <div class="layui-card-body">
                <table id="gridMBean" lay-filter="gridMBean"></table>

                <script type="text/html" id="colMeanRate">
                    {{#  if(d.dblMeanRate > 0){ }}
                    <span class="layui-badge layui-bg-blue">{{ d.meanRate }}</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-orange">{{ d.meanRate }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colOneMinute">
                    {{#  if(d.dblOneMinute > 0){ }}
                    <span class="layui-badge layui-bg-blue">{{ d.oneMinute }}</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-orange">{{ d.oneMinute }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colFiveMinute">
                    {{#  if(d.dblFiveMinute > 0){ }}
                    <span class="layui-badge layui-bg-blue">{{ d.fiveMinute }}</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-orange">{{ d.fiveMinute }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colFifteenMinute">
                    {{#  if(d.dblFifteenMinute > 0){ }}
                    <span class="layui-badge layui-bg-blue">{{ d.fifteenMinute }}</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-orange">{{ d.fifteenMinute }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colTopicName">
                    {{#  if(d.logsize >= 0){ }}
                    {{ d.topicName }}
                    {{#  } else { }}
                    <span class="layui-badge">{{ d.topicName }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colHost">
                    {{#  if(d.logsize >= 0){ }}
                    {{ d.partitionId }}
                    {{#  } else { }}
                    <span class="layui-badge">{{ d.partitionId }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colLogsize">
                    {{#  if(d.logsize >= 0){ }}
                    {{ d.logsize }}
                    {{#  } else { }}
                    <span class="layui-badge">{{ d.logsize }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colLeader">
                    {{#  if(d.logsize >= 0){ }}
                    {{ d.strLeader }}
                    {{#  } else { }}
                    <span class="layui-badge">{{ d.strLeader }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colReplicas">
                    {{#  if(d.logsize >= 0){ }}
                    {{ d.strReplicas }}
                    {{#  } else { }}
                    <span class="layui-badge">{{ d.strReplicas }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colIsr">
                    {{#  if(d.logsize >= 0){ }}
                    {{ d.strIsr }}
                    {{#  } else { }}
                    <span class="layui-badge">{{ d.strIsr }}</span>
                    {{#  } }}
                </script>

            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
            const admin = layui.admin, table = layui.table, $ = layui.$;
            table.render({
                elem: '#grid',
                url: 'listTopicDetails?topicName=${topicName}',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                even: true,
                text: {none: '暂无相关数据'},
                cols: [[
                    {field: 'topicName', title: '主题名称', templet: '#colTopicName', width: 200},
                    {field: "partitionId", title: '分区号', templet: '#colHost', width: 100},
                    {field: "logsize", title: '消息数量', templet: '#colLogsize', width: 100},
                    {field: "strLeader", title: '分区Leader', templet: '#colLeader'},
                    {field: "strReplicas", title: '分区副本', templet: '#colReplicas'},
                    {field: "strIsr", title: '同步副本', templet: '#colIsr'}
                ]],
                done: function (res) {
                    let logsize = 0;
                    for (let i = 0; i < res.data.length; i++) {
                        if (res.data[i].logsize) {
                            logsize += res.data[i].logsize;
                        }
                    }
                    admin.post("listTopicSize", {"topicName": "${topicName}"}, function (res) {
                        $("#divGridtHeader").html("共有<span class=\"layui-badge layui-bg-blue\">" + logsize.toString() + "</span>条消息, " + "体积为<span class=\"layui-badge layui-bg-blue\">" + res.data + "</span>&nbsp;&nbsp;&nbsp;&nbsp;<button id=\"btnRefresh\" type=\"button\" class=\"layui-btn layui-btn-xs\">刷新</button>");
                        $("#btnRefresh").click(function () {
                            table.reload('grid');
                            table.reload('gridMBean');
                        });
                    })
                }
            });

            table.render({
                elem: '#gridMBean',
                url: 'listTopicMBean?topicName=${topicName}',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                even: true,
                text: {none: '暂无相关数据'},
                cols: [[
                    {field: 'name', title: '说明'},
                    {field: "meanRate", title: '平均值', templet: '#colMeanRate'},
                    {field: "oneMinute", title: '每分钟', templet: '#colOneMinute', width: 300},
                    {field: "fiveMinute", title: '每5分钟', templet: '#colFiveMinute', width: 300},
                    {field: "fifteenMinute", title: '每15分钟', templet: '#colFifteenMinute', width: 300}
                ]]
            });
        });
    </script>
    </body>
    </html>
</@compress>