<@compress>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <script src="${ctx}/js/dashboard.js"></script>
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-row layui-col-space15">
            <div class="layui-col-md6">
                <div class="layui-card">
                    <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                        <div class="layui-form-item">
                            <div class="layui-inline">主题名称</div>
                            <div class="layui-inline" style="width:265px">
                                <select id="tpsTopicName" name="tpsTopicName" lay-filter="tpsTopicName"
                                        lay-verify="required" lay-search>
                                    <option value="所有主题">所有主题</option>
                                    <#list topics as topic>
                                        <option value="${topic.topicName}">${topic.topicName}</option>
                                    </#list>
                                </select>
                            </div>

                            <div class="layui-inline">时间范围</div>
                            <div class="layui-inline" style="width:300px">
                                <input type="text" id="topicCreateTimeRange" name="topicCreateTimeRange"
                                       lay-verify="required"
                                       class="layui-input" placeholder="请选择时间范围" autocomplete="off">
                            </div>
                            <button id="btnTopicRefresh" type="button" class="layui-btn layui-btn-xs"
                                    style="float: right;margin-top: 10px">刷 新
                            </button>
                        </div>
                    </div>
                    <div class="layui-card-body">
                        <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                             lay-filter="LAY-index-normcol">
                            <div carousel-item id="topicChart">
                                <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="layui-card">
                    <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                        <div class="layui-form-item">
                            <div class="layui-inline">时间范围</div>
                            <div class="layui-inline" style="width:300px">
                                <input type="text" id="rankCreateTimeRange" name="rankCreateTimeRange"
                                       lay-verify="required"
                                       class="layui-input" placeholder="请选择时间范围" autocomplete="off">
                            </div>
                            <button id="btnTopicRankRefresh" type="button" class="layui-btn layui-btn-xs"
                                    style="float: right;margin-top: 10px">刷 新
                            </button>
                        </div>
                    </div>
                    <div class="layui-card-body">
                        <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                             lay-filter="LAY-index-heapcol">
                            <div carousel-item id="topicRankChart">
                                <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="layui-col-md6">
                <div class="layui-card">
                    <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                        <div class="layui-form-item">
                            <div class="layui-inline">消费组名称</div>
                            <div class="layui-inline" style="width:265px">
                                <select id="consumerName" name="consumerName" lay-filter="consumerName"
                                        lay-verify="required" lay-search>
                                    <option value="">请选择消费组</option>
                                    <#list consumers as consumer>
                                        <option value="${consumer.groupId}">${consumer.groupId}</option>
                                    </#list>
                                </select>
                            </div>

                            <div class="layui-inline">时间范围</div>
                            <div class="layui-inline" style="width:300px">
                                <input type="text" id="lagCreateTimeRange" name="lagCreateTimeRange"
                                       lay-verify="required"
                                       class="layui-input" placeholder="请选择时间范围" autocomplete="off">
                            </div>
                            <button id="btnLagRefresh" type="button" class="layui-btn layui-btn-xs"
                                    style="float: right;margin-top: 10px">刷 新
                            </button>
                        </div>
                    </div>
                    <div class="layui-card-body">
                        <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                             lay-filter="LAY-index-normcol">
                            <div carousel-item id="lagChart">
                                <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="layui-card">
                    <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                        <div class="layui-form-item">
                            <div class="layui-inline">主题名称</div>
                            <div class="layui-inline" style="width:265px">
                                <select id="hisTopicName" name="hisTopicName" lay-filter="hisTopicName"
                                        lay-verify="required" lay-search>
                                    <option value="">请选择主题</option>
                                    <#list topics as topic>
                                        <option value="${topic.topicName}">${topic.topicName}</option>
                                    </#list>
                                </select>
                            </div>
                            <button id="btnTopicHisRefresh" type="button" class="layui-btn layui-btn-xs"
                                    style="float: right;margin-top: 10px">刷 新
                            </button>
                        </div>
                    </div>
                    <div class="layui-card-body">
                        <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                             lay-filter="LAY-index-heapcol">
                            <div carousel-item id="topicHistoryChart">
                                <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'laydate', 'carousel', 'echarts'], function () {
            const admin = layui.admin, form = layui.form, laydate = layui.laydate, echarts = layui.echarts;
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

                function refreshTopicTpsChart() {
                    const tpsTopicName = $.trim($("#tpsTopicName").siblings().find("dd[class='layui-this']").html());
                    const topicCreateTimeRange = $.trim($("#topicCreateTimeRange").val());
                    if (tpsTopicName == null || tpsTopicName === '' || topicCreateTimeRange == null || topicCreateTimeRange === '') {
                        return;
                    }

                    admin.post("getTopicChart?topicName=" + tpsTopicName + "&createTimeRange=" + topicCreateTimeRange, {}, function (data) {
                        _init(data.data);
                    });

                    function _init(data) {
                        const ele = $('#topicChart').children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        const echart = echarts.init(ele[0], layui.echartsTheme);
                        echart.setOption(topicChart(data));
                        window.onresize = echart.resize;
                    }
                }

                function refreshLagChart() {
                    const consumerId = $.trim($("#consumerName").siblings().find("dd[class='layui-this']").html());
                    const lagCreateTimeRange = $.trim($("#lagCreateTimeRange").val());
                    if (consumerId == null || consumerId === '' || lagCreateTimeRange == null || lagCreateTimeRange === '') {
                        _init({topicNames: [], times: [], series: []});
                        return;
                    }

                    admin.post("getLagChart?groupId=" + consumerId + "&createTimeRange=" + lagCreateTimeRange, {}, function (data) {
                        _init(data.data);
                    });

                    function _init(data) {
                        const ele = $('#lagChart').children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        const echart = echarts.init(ele[0], layui.echartsTheme);
                        echart.setOption(lagChart(data));
                        window.onresize = echart.resize;
                    }
                }

                function refreshTopicRankChart() {
                    const rankCreateTimeRange = $.trim($("#rankCreateTimeRange").val());
                    admin.post("getTopicRankChart?createTimeRange=" + rankCreateTimeRange, {}, function (data) {
                        _init(data.data);
                    });

                    function _init(data) {
                        const ele = $('#topicRankChart').children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        const echart = echarts.init(ele[0], layui.echartsTheme);
                        echart.setOption(topicRankChart(data));
                        window.onresize = echart.resize;
                    }
                }

                function refreshTopicHistoryChart() {
                    const hisTopicName = $.trim($("#hisTopicName").siblings().find("dd[class='layui-this']").html());
                    if (hisTopicName == null || hisTopicName === '') {
                        return;
                    }
                    admin.post("getTopicHistoryChart?topicName=" + hisTopicName, {}, function (data) {
                        _init(data.data);
                    });

                    function _init(data) {
                        const ele = $('#topicHistoryChart').children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        const echart = echarts.init(ele[0], layui.echartsTheme);
                        echart.setOption(topicHistoryChart(data));
                        window.onresize = echart.resize;
                    }
                }

                laydate.render({
                    elem: '#lagCreateTimeRange',
                    type: 'datetime',
                    range: true,
                    min: -${savingDays-1},
                    max: 1,
                    done: function () {
                        refreshLagChart();
                    }
                });

                laydate.render({
                    elem: '#topicCreateTimeRange',
                    type: 'datetime',
                    range: true,
                    min: -${savingDays-1},
                    max: 1,
                    done: function () {
                        refreshTopicTpsChart();
                    }
                });

                laydate.render({
                    elem: '#rankCreateTimeRange',
                    type: 'datetime',
                    range: true,
                    min: -${savingDays-1},
                    max: 1,
                    done: function () {
                        refreshTopicRankChart();
                    }
                });

                const now = new Date();
                now.setDate(now.getDate() + 1);
                const to = now.format('yyyy-MM-dd' + ' 00:00:00');
                now.setDate(now.getDate() - 1);
                now.setMinutes(now.getMinutes() - 30);
                const from = now.format('yyyy-MM-dd HH:mm' + ':00');
                $("#lagCreateTimeRange,#topicCreateTimeRange,#rankCreateTimeRange").val(from + ' - ' + to);

                form.on('select(consumerName)', function () {
                    refreshLagChart();
                });

                $("#lagCreateTimeRange").change(function () {
                    refreshLagChart();
                });

                $("#btnLagRefresh").click(function () {
                    refreshLagChart();
                });

                form.on('select(tpsTopicName)', function () {
                    refreshTopicTpsChart();
                });

                $("#topicCreateTimeRange").change(function () {
                    refreshTopicTpsChart();
                });

                $("#btnTopicRefresh").click(function () {
                    refreshTopicTpsChart();
                });

                $("#btnTopicRankRefresh").click(function () {
                    refreshTopicRankChart();
                });

                form.on('select(hisTopicName)', function () {
                    refreshTopicHistoryChart();
                });

                $("#btnTopicHisRefresh").click(function () {
                    refreshTopicHistoryChart();
                });

                $('#tpsTopicName option:eq(1)').attr('selected', 'selected');
                $('#consumerName option:eq(1)').attr('selected', 'selected');
                $('#hisTopicName option:eq(1)').attr('selected', 'selected');
                layui.form.render('select');

                refreshTopicTpsChart();
                refreshLagChart();
                refreshTopicRankChart();
                refreshTopicHistoryChart();
            });
        });
    </script>
    </body>
    </html>
</@compress>