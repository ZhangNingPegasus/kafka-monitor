<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>
    <div class="layui-fluid">
        <div class="layui-card">
            <div class="layui-card-header">ZooKeeper集群</div>
            <div class="layui-card-body">
                <table id="zkGrid" lay-filter="zkGrid"></table>
                <script type="text/html" id="colMode">
                    <span class="layui-badge layui-bg-green">{{ d.mode }}</span>
                </script>
            </div>
        </div>
        <div class="layui-card">
            <div class="layui-card-header">Kafka集群</div>
            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>
                <script type="text/html" id="colVersion">
                    <span class="layui-badge layui-bg-green">{{ d.version }}</span>
                </script>
            </div>
        </div>
        <div class="layui-card">
            <div class="layui-card-header">Kafka集群</div>
            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normline">
                    <div carousel-item id="kafkaCluster">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
            const admin = layui.admin, table = layui.table, $ = layui.$, echarts = layui.echarts;
            layui.use(['carousel'], function () {
                const $ = layui.$, carousel = layui.carousel, element = layui.element, device = layui.device();
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

            table.render({
                elem: '#zkGrid',
                url: 'listZk',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {type: 'numbers', title: '序号', width: 200},
                    {field: 'host', title: '地址'},
                    {field: 'port', title: '端口', width: 255},
                    {field: 'version', title: '版本', templet: '#colVersion', width: 255},
                    {field: 'mode', title: '模式', templet: '#colMode', width: 240}
                ]]
            });

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
                    {field: 'name', title: '编号', width: 200},
                    {field: 'host', title: '地址'},
                    {field: 'port', title: '端口', width: 225},
                    {field: 'jmxPort', title: 'JMX端口', width: 225},
                    {field: 'createTime', title: '创建时间', width: 200},
                    {field: 'version', title: '版本', templet: '#colVersion', width: 150}
                ]],
                done: function () {
                    admin.post("getChartData", {}, function (data) {
                        const echnormline = [], elemnormline = $('#kafkaCluster').children('div'),
                            rendernormline = function (index) {
                                echnormline[index] = echarts.init(elemnormline[index], layui.echartsTheme);
                                echnormline[index].setOption({
                                    tooltip: {
                                        trigger: 'item',
                                        triggerOn: 'mousemove'
                                    },
                                    series: [
                                        {
                                            type: 'tree',
                                            data: [data.data],
                                            top: '1%',
                                            left: '7%',
                                            bottom: '1%',
                                            right: '12%',
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
                                window.onresize = echnormline[index].resize;
                            };
                        if (!elemnormline[0]) return;
                        rendernormline(0);
                    });
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>