<@compress>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <script src="${ctx}/js/zkperformance.js"></script>
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-card">
            <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                <div class="layui-form-item">
                    <div class="layui-inline">时间范围</div>
                    <div class="layui-inline" style="width:300px">
                        <input type="text" id="zkTimeRange" name="zkTimeRange"
                               lay-verify="required" class="layui-input" placeholder="请选择时间范围"
                               autocomplete="off">
                    </div>
                    <button id="btnRefresh" type="button" class="layui-btn layui-btn-sm">
                        刷 新
                    </button>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="zkAlivedChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="zkSendChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="zkReceiveChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="zkQueueChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <script type="text/javascript">
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'laydate', 'carousel', 'echarts'], function () {
            const admin = layui.admin, laydate = layui.laydate, echarts = layui.echarts;
            layui.use(['carousel'], function () {
                const $ = layui.$, carousel = layui.carousel, element = layui.element, device = layui.device(),
                    timeRange = $("#zkTimeRange");

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

                function refreshChart() {
                    const zkTimeRange = $.trim(timeRange.val());
                    if (zkTimeRange == null || zkTimeRange === '') {
                        _initChart('ZooKeeper连接数图', 'zkAlivedChart', {times: [], series: []});
                        _initChart('ZooKeeper数据包发送图', 'zkAlivedChart', {times: [], series: []});
                        _initChart('ZooKeeper数据包接受图', 'zkReceiveChart', {times: [], series: []});
                        _initChart('ZooKeeper排队请求数量图', 'zkQueueChart', {times: [], series: []});
                        return;
                    }

                    admin.post("getChart", {'createTimeRange': zkTimeRange}, function (data) {
                        _initChart('ZooKeeper连接数图', 'zkAlivedChart', data.data.alive);
                        _initChart('ZooKeeper数据包发送图', 'zkSendChart', data.data.send);
                        _initChart('ZooKeeper数据包接受图', 'zkReceiveChart', data.data.received);
                        _initChart('ZooKeeper排队请求数量图', 'zkQueueChart', data.data.queue);
                    });

                    function _initChart(title, eleId, data) {
                        const ele = $('#' + eleId).children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        const echart = echarts.init(ele[0], layui.echartsTheme);
                        echart.setOption(zkChart(title, data));
                        window.onresize = echart.resize;
                    }
                }

                laydate.render({
                    elem: '#zkTimeRange',
                    type: 'datetime',
                    range: true,
                    min: -${savingDays-1},
                    max: 1,
                    done: function () {
                        refreshChart();
                    }
                });

                const now = new Date();
                now.setDate(now.getDate() + 1);
                const to = now.format('yyyy-MM-dd' + ' 00:00:00');
                now.setDate(now.getDate() - 1);
                now.setMinutes(now.getMinutes() - 60);
                const from = now.format('yyyy-MM-dd HH:mm' + ':00');
                timeRange.val(from + ' - ' + to);

                $("#btnRefresh").click(function () {
                    refreshChart();
                });

                timeRange.change(function () {
                    refreshChart();
                });

                refreshChart();
            });
        });
    </script>
    </body>
    </html>
</@compress>