<@compress>
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
                <script type="text/html" id="grid-bar">
                    <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="getDetail"><i
                                class="layui-icon layui-icon-read"></i>查看</a>
                    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del"><i
                                class="layui-icon layui-icon-delete"></i>删除</a>
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

        <div class="layui-card">
            <div class="layui-card-header">【消费组 - 主题】对应关系图</div>
            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normline">
                    <div carousel-item id="activeTopics">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
            const admin = layui.admin, table = layui.table, $ = layui.$, echarts = layui.echarts;
            //区块轮播切换
            layui.use(['carousel'], function () {
                const $ = layui.$, carousel = layui.carousel, element = layui.element, device = layui.device();
                //轮播切换
                $('.layadmin-carousel').each(function () {
                    const othis = $(this);
                    carousel.render({
                        elem: this,
                        width: '100%',
                        arrow: 'none',
                        interval: othis.data('interval'),
                        autoplay: othis.data('autoplay') === true,
                        trigger: (device.ios || device.android) ? 'click' : 'hover',
                        anim: othis.data('anim')
                    });
                });
                element.render('progress');
            });

            function refresChart() {
                admin.post("getChartData", {}, function (data) {
                    _init(data.data);
                });

                function _init(data) {
                    const ele = $('#activeTopics').children('div');
                    ele.removeAttr("_echarts_instance_").empty();
                    const echart = echarts.init(ele[0], layui.echartsTheme);
                    if (!data) {
                        return;
                    }
                    echart.setOption({
                        tooltip: {
                            trigger: 'item',
                            triggerOn: 'mousemove'
                        },
                        series: [
                            {
                                type: 'tree',
                                data: [data],
                                top: '1%',
                                left: '7%',
                                bottom: '1%',
                                right: '27%',
                                symbolSize: 7,
                                label: {
                                    normal: {
                                        position: 'left',
                                        verticalAlign: 'middle',
                                        align: 'right',
                                        fontSize: 14
                                    }
                                },
                                leaves: {
                                    label: {
                                        normal: {
                                            position: 'right',
                                            verticalAlign: 'middle',
                                            align: 'left'
                                        }
                                    }
                                },
                                expandAndCollapse: true,
                                animationDuration: 550,
                                animationDurationUpdate: 750
                            }
                        ]
                    });
                    window.onresize = echart.resize;
                }
            }


            table.render({
                elem: '#grid',
                url: 'list',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'groupId', title: '组名称', templet: "#colGroupId"},
                    {field: 'node', title: '节点', width: 180},
                    {title: '订阅主题数', sort: true, templet: "#topicCount", width: 400},
                    {title: '活跃主题', sort: true, templet: "#activeTopicCount", width: 400},
                    {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 145}
                ]],
                done: function () {
                    $("a[class='groupid layui-table-link']").click(function () {
                        showDetail($(this).attr("data"));
                    });
                    refresChart();
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