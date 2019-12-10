<@compress>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <script src="${ctx}/js/kafkaperformance.js"></script>
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-card">
            <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                <div class="layui-form-item">
                    <div class="layui-inline">时间范围</div>
                    <div class="layui-inline" style="width:300px">
                        <input type="text" id="kafkaTimeRange" name="kafkaTimeRange"
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
                    <div carousel-item id="osUsedMemoryChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="msgInChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="bytesInChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="bytesOutChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="bytesRejectedChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="failedFetchRequestChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="failedProduceRequestChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="produceMessageConversionsChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="totalFetchRequestsChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="totalProduceRequestsChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="replicationBytesOutChart">
                        <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                     lay-filter="LAY-index-normcol">
                    <div carousel-item id="replicationBytesInChart">
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
                    timeRange = $("#kafkaTimeRange");
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
                    const zkSendTimeRange = $.trim(timeRange.val());
                    if (zkSendTimeRange == null || zkSendTimeRange === '') {
                        _initChart('Message In (per/sec)', 'msgInChart', {times: [], series: []});
                        _initChart('Topic Byte In (byte/sec)', 'bytesInChart', {times: [], series: []});
                        _initChart('Topic Byte Out (byte/sec)', 'bytesOutChart', {times: [], series: []});
                        _initChart('Byte Rejected (/sec)', 'bytesRejectedChart', {times: [], series: []});
                        _initChart('Failed Fetch Request (/sec)', 'failedFetchRequestChart', {times: [], series: []});
                        _initChart('Failed Produce Request (/sec)', 'failedProduceRequestChart', {
                            times: [],
                            series: []
                        });
                        _initChart('Produce Message Conversions (/sec)', 'produceMessageConversionsChart', {
                            times: [],
                            series: []
                        });
                        _initChart('Total Fetch Requests (/sec)', 'totalFetchRequestsChart', {times: [], series: []});
                        _initChart('Total Produce Requests (/sec)', 'totalProduceRequestsChart', {
                            times: [],
                            series: []
                        });
                        _initChart('Replication Bytes Out (byte/sec)', 'replicationBytesOutChart', {
                            times: [],
                            series: []
                        });
                        _initChart('Replication Bytes In (byte/sec)', 'replicationBytesInChart', {
                            times: [],
                            series: []
                        });
                        _initChart('内存已使用百分比', 'osUsedMemoryChart', {times: [], series: []});
                        return;
                    }

                    admin.post("getChart", {'createTimeRange': zkSendTimeRange}, function (data) {
                        _initChart('Message In (per/sec)', 'msgInChart', data.data.msgIn);
                        _initChart('Topic Byte In (byte/sec)', 'bytesInChart', data.data.bytesIn);
                        _initChart('Topic Byte Out (byte/sec)', 'bytesOutChart', data.data.bytesOut);
                        _initChart('Byte Rejected (/sec)', 'bytesRejectedChart', data.data.bytesRejected);
                        _initChart('Failed Fetch Request (/sec)', 'failedFetchRequestChart', data.data.failedFetchRequest);
                        _initChart('Failed Produce Request (/sec)', 'failedProduceRequestChart', data.data.failedProduceRequest);
                        _initChart('Produce Message Conversions (/sec)', 'produceMessageConversionsChart', data.data.produceMessageConversions);
                        _initChart('Total Fetch Requests (/sec)', 'totalFetchRequestsChart', data.data.totalFetchRequests);
                        _initChart('Total Produce Requests (/sec)', 'totalProduceRequestsChart', data.data.totalProduceRequests);
                        _initChart('Replication Bytes Out (byte/sec)', 'replicationBytesOutChart', data.data.replicationBytesOut);
                        _initChart('Replication Bytes In (byte/sec)', 'replicationBytesInChart', data.data.replicationBytesIn);
                        _initChart('内存已使用百分比', 'osUsedMemoryChart', data.data.osFreeMemory, true);
                    });


                    function _initChart(title, eleId, data, isPercent) {
                        const ele = $('#' + eleId).children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        const echart = echarts.init(ele[0], layui.echartsTheme);
                        echart.setOption(kafkaChart(title, data, isPercent));
                        window.onresize = echart.resize;
                    }


                }

                laydate.render({
                    elem: '#kafkaTimeRange',
                    type: 'datetime',
                    range: true,
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