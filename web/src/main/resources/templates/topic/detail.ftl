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
                <table id="gridLogSize" lay-filter="gridLogSize"></table>
            </div>

            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>
            </div>
        </div>

        <div class="layui-card">
            <div class="layui-card-header">主题其他信息</div>
            <div class="layui-card-body">
                <table id="gridMBean" lay-filter="gridMBean"></table>

                <script type="text/html" id="colMeanRate">
                    <span class="layui-badge layui-bg-blue">{{ d.meanRate }}</span>
                </script>

                <script type="text/html" id="colOneMinute">
                    <span class="layui-badge layui-bg-blue">{{ d.oneMinute }}</span>
                </script>

                <script type="text/html" id="colFiveMinute">
                    <span class="layui-badge layui-bg-blue">{{ d.fiveMinute }}</span>
                </script>

                <script type="text/html" id="colFifteenMinute">
                    <span class="layui-badge layui-bg-blue">{{ d.fifteenMinute }}</span>
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

                <script type="text/html" id="colDay0LogSize">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.day0LogSize }}</span>
                    {{#  } else { }}
                    {{ d.day0LogSize }}
                    {{#  } }}
                </script>
                <script type="text/html" id="colDay1LogSize">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.day1LogSize }}</span>
                    {{#  } else { }}
                    {{ d.day1LogSize }}
                    {{#  } }}
                </script>
                <script type="text/html" id="colDay2LogSize">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.day2LogSize }}</span>
                    {{#  } else { }}
                    {{ d.day2LogSize }}
                    {{#  } }}
                </script>
                <script type="text/html" id="colDay3LogSize">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.day3LogSize }}</span>
                    {{#  } else { }}
                    {{ d.day3LogSize }}
                    {{#  } }}
                </script>
                <script type="text/html" id="colDay4LogSize">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.day4LogSize }}</span>
                    {{#  } else { }}
                    {{ d.day4LogSize }}
                    {{#  } }}
                </script>
                <script type="text/html" id="colDay5LogSize">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.day5LogSize }}</span>
                    {{#  } else { }}
                    {{ d.day5LogSize }}
                    {{#  } }}
                </script>
                <script type="text/html" id="colDay6LogSize">
                    {{#  if(d.error || d.logSize < 0){ }}
                    <span title="{{ d.error }}" class="layui-badge">{{ d.day6LogSize }}</span>
                    {{#  } else { }}
                    {{ d.day6LogSize }}
                    {{#  } }}
                </script>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
            const admin = layui.admin, table = layui.table, $ = layui.$;
            tableErrorHandler();
            table.render({
                elem: '#grid',
                url: 'listTopicDetails?topicName=${topicName}',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                even: true,
                text: {none: '暂无相关数据'},
                cols: [[
                    {field: "partitionId", title: '分区号', templet: '#colHost', width: 80},
                    {field: "logsize", title: '消息数量', templet: '#colLogsize', width: 90},
                    {field: "strLeader", title: '分区Leader', templet: '#colLeader', width: 200},
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
                        $("#divGridtHeader").html("主题<span class=\"layui-badge layui-bg-gray\">${topicName}</span>共有<span class=\"layui-badge layui-bg-blue\">" + logsize.toString() + "</span>条消息, " + "体积为<span class=\"layui-badge layui-bg-blue\">" + res.data + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<button id=\"btnRefresh\" type=\"button\" class=\"layui-btn layui-btn-xs\">&nbsp;&nbsp;刷&nbsp;新&nbsp;&nbsp;</button>");
                        $("#btnRefresh").click(function () {
                            reloadGrid();
                        });
                    }, function () {
                        $("#divGridtHeader").html("主题<span class=\"layui-badge layui-bg-gray\">${topicName}</span>共有<span class=\"layui-badge layui-bg-blue\">" + logsize.toString() + "</span>条消息" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<button id=\"btnRefresh\" type=\"button\" class=\"layui-btn layui-btn-xs\">&nbsp;&nbsp;刷&nbsp;新&nbsp;&nbsp;</button>");
                        $("#btnRefresh").click(function () {
                            reloadGrid();
                        });
                    });
                }
            });

            table.render({
                elem: '#gridLogSize',
                url: 'listTopicLogSize?topicName=${topicName}',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                even: true,
                text: {none: '暂无相关数据'},
                cols: [[
                    {field: "day0LogSize", title: '今天消息数量', templet: '#colDay0LogSize'},
                    {field: "day1LogSize", title: '昨天消息数量', templet: '#colDay1LogSize'},
                    {field: "day2LogSize", title: '前天消息数量', templet: '#colDay2LogSize'},
                    {field: "day3LogSize", title: '前3天消息数量', templet: '#colDay3LogSize'},
                    {field: "day4LogSize", title: '前4天消息数量', templet: '#colDay4LogSize'},
                    {field: "day5LogSize", title: '前5天消息数量', templet: '#colDay5LogSize'},
                    {field: "day6LogSize", title: '前6天消息数量', templet: '#colDay6LogSize'}
                ]],
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

            function reloadGrid() {
                table.reload('grid');
                table.reload('gridLogSize');
                table.reload('gridMBean');
            }

        });
    </script>
    </body>
    </html>
</@compress>