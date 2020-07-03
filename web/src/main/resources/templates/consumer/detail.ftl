<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-card">
            <div id="divGridtHeader" class="layui-card-header">消费组名称: <span class="layui-badge layui-bg-blue">${groupId}</span></div>
            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>

                <script type="text/html" id="colTopicName">
                    {{#  if(d.error){ }}
                    <a title="{{ d.error }}" href="javascript:void(0)" class="topicName layui-table-link">
                        <span class="layui-badge">{{ d.topicName }}</span>
                    </a>
                    {{#  } else { }}
                    <a href="javascript:void(0)" class="topicName layui-table-link">{{ d.topicName }}</a>
                    {{#  } }}
                </script>

                <script type="text/html" id="colLag">
                    {{#  if(d.consumerStatus != 1){ }}
                    <span class="layui-badge">{{ d.lag }}</span>
                    {{#  } else if(d.lag > 0) { }}
                    <span class="layui-badge layui-bg-orange">{{ d.lag }}</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-green">{{ d.lag }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colConsumerStatus">
                    {{#  if(d.consumerStatus == 1){ }}
                    <span class="layui-badge layui-bg-green">已上线</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-orange">已下线</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="grid-bar">
                    {{#  if(d.lag >= 0){ }}
                    <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="getOffset"><i
                                class="layui-icon layui-icon-read"></i>查看偏移量</a>
                    {{#  } }}
                </script>
            </div>
        </div>

        <div id="divGridOffset" class="layui-card" style="display: none">
            <div id="divGridOffsetHeader" class="layui-card-header"></div>
            <div class="layui-card-body">
                <table id="gridOffset" lay-filter="gridOffset"></table>

                <script type="text/html" id="colLogsize">
                    {{#  if(d.logSize < 0){ }}
                    <span class="layui-badge">{{ d.logSize }}</span>
                    {{#  } else if(d.consumerId || d.offset) { }}
                    <span class="layui-badge layui-bg-green">{{ d.logSize }}</span>
                    {{#  } else { }}
                    <span class="layui-badge">0</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colDetailLag">
                    {{#  if(d.logSize < 0){ }}
                    <span class="layui-badge">{{ d.lag }}</span>
                    {{#  } else if(d.lag > 0) { }}
                    <span class="layui-badge layui-bg-orange">{{ d.lag }}</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-green">{{ d.lag }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colOffset">
                    {{#  if(d.logSize < 0){ }}
                    <span class="layui-badge">{{ d.offset }}</span>
                    {{#  } else if(d.consumerId || d.offset) { }}
                    <span class="layui-badge layui-bg-green">{{ d.offset }}</span>
                    {{#  } else { }}
                    <span class="layui-badge">{{ d.logSize }}</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="colConsumerId">
                    {{#  if(d.logSize < 0){ }}
                    <span class="layui-badge">{{ d.consumerId }}</span>
                    {{#  } else { }}
                    {{ d.consumerId }}
                    {{#  } }}
                </script>

            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
            const table = layui.table, $ = layui.$;
            tableErrorHandler();
            table.render({
                elem: '#grid',
                url: 'listConsumerDetails?groupId=${groupId}',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'topicName', title: '主题名称', templet: '#colTopicName', width: 500},
                    {field: "lag", title: '堆积消息数量', templet: '#colLag', width: 200},
                    {title: '消费状态', templet: '#colConsumerStatus', width: 300},
                    {fixed: 'right', title: '操作', toolbar: '#grid-bar'}
                ]],
                done: function (res) {
                    $("a[class='topicName layui-table-link']").click(function () {
                        showOffsetDetails($(this).text());
                    });
                    if (res.data.length === 1) {
                        showOffsetDetails(res.data[0].topicName);
                    }
                }
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'getOffset') {
                    showOffsetDetails(data.topicName);
                }
            });

            function showOffsetDetails(topicName) {
                table.render({
                    elem: '#gridOffset',
                    url: 'listOffsetVo?groupId=${groupId}&topicName=' + topicName,
                    method: 'post',
                    cellMinWidth: 80,
                    page: false,
                    even: true,
                    text: {
                        none: '暂无相关数据'
                    },
                    cols: [[
                        {field: 'partitionId', title: '分区号', width: 100},
                        {field: 'logSize', templet: "#colLogsize", title: '消息数量', width: 200},
                        {field: 'offset', templet: "#colOffset", title: '已消费数量', width: 200},
                        {field: 'lag', title: '未消费数量', templet: "#colDetailLag", width: 200},
                        {field: 'consumerId', templet: "#colConsumerId", title: '消费者ID'}
                    ]],
                    done: function (res) {
                        if (!res.data) {
                            return;
                        }
                        let logsize = 0, offset = 0, lag = 0;
                        let cls = "";
                        for (let i = 0; i < res.data.length; i++) {
                            if (res.data[i].logSize && res.data[i].logSize >= 0) logsize += res.data[i].logSize;
                            if (res.data[i].offset && res.data[i].offset >= 0) offset += res.data[i].offset;
                            if (res.data[i].lag && res.data[i].lag >= 0) lag += res.data[i].lag;
                            console.log(res.data[i]);
                            if (res.data[i].consumerId === "") {
                                cls = "";
                            } else if (lag > 0) {
                                cls = "layui-bg-orange";
                            } else {
                                cls = "layui-bg-green";
                            }
                        }
                        $("#divGridOffsetHeader").html("主题：" + "<span class=\"layui-badge layui-bg-blue\">" +topicName + "</span>, 分区数:<span class=\"layui-badge layui-bg-blue\">" + res.data.length + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;总共消息" + "<span class=\"layui-badge layui-bg-blue\">" + logsize + "</span>条, 已消费" + "<span class=\"layui-badge layui-bg-green\">" + offset + "</span>条。" + "  当前有<span class='layui-badge " + cls + "'><b><i>" + lag + "</i></b></span>条信息堆积");
                        $("td[data-field=topicName]").each(function (a, td) {
                            if ($.trim(topicName) === $.trim($(td).find("div").text())) {
                                $(td).siblings("td[data-field=lag]").find("div > span").attr("class", "layui-badge " + cls);

                                $(td).siblings("td[data-field=lag]").find("div > span").html(lag);
                            }
                        });

                    }
                });
                $("#divGridOffset").show();
            }

        });
    </script>

    </body>
    </html>
</@compress>