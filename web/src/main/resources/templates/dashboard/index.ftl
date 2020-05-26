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
            <div class="layui-col-md12">
                <div class="layui-card layui-form layui-card-header layuiadmin-card-header-auto">
                    <div class="layui-form-item">
                        <div class="layui-inline">主题名称</div>
                        <div class="layui-inline" style="width:350px">
                            <select id="topicName" name="topicName" lay-filter="topicName" lay-verify="required"
                                    lay-search>
                                <option value="">请选择主题</option>
                                <#list topics as topic>
                                    <option value="${topic}">${topic}</option>
                                </#list>
                            </select>
                        </div>

                        <div class="layui-inline">消费组名称</div>
                        <div class="layui-inline" style="width:350px">
                            <select id="consumerName" name="consumerName" lay-filter="consumerName"
                                    lay-verify="required"
                                    lay-search>
                                <option value="">请选择消费组名称</option>
                            </select>
                        </div>

                        <div class="layui-inline">时间范围</div>
                        <div class="layui-inline" style="width:300px">
                            <input type="text" id="timeRange" name="timeRange" lay-verify="required" class="layui-input"
                                   placeholder="请选择时间范围" autocomplete="off">
                        </div>

                        <button id="btnTopicRefresh" type="button" class="layui-btn layui-btn-radius"
                                style="height: 38px;margin-top: -5px;">&nbsp;&nbsp;刷&nbsp;&nbsp;新&nbsp;&nbsp;
                        </button>

                        <span style="float: right">
                            <input id="btnAutoRefresh" lay-filter="btnAutoRefresh"
                                   type="checkbox" name="是[否]"
                                   title=" 自 动 刷 新 " checked="checked"/>
                        </span>
                    </div>

                </div>
            </div>
        </div>

        <div class="layui-row layui-col-space15">
            <div class="layui-col-md6">
                <div class="layui-card">

                    <div class="layui-card-body">
                        <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                             lay-filter="LAY-index-normcol">
                            <div carousel-item id="topicSendChart">
                                <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="layui-card">
                    <div class="layui-card-body">
                        <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                             lay-filter="LAY-index-normcol">
                            <div carousel-item id="consumeTpsChart">
                                <div><i class="layui-icon layui-icon-loading1 layadmin-loading"></i></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="layui-card">
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

                $(".layui-icon.layui-icon-ok").hide();

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

                form.on('select(topicName)', function (data) {
                    const topicName = data.value;
                    $("select[name=consumerName]").html("<option value=\"\">请选择消费组名称</option>");
                    admin.post("getConsumersByTopicName", {'topicName': topicName}, function (res) {
                        $.each(res.data, function (key, val) {
                            const option = $("<option>").val(val.groupId).text(val.groupId);
                            $("select[name=consumerName]").append(option);
                        });
                        $('#consumerName option:eq(1)').attr('selected', 'selected');
                        layui.form.render('select');
                        refreshLagChart();
                        refreshConsumeTpsChart();
                        refreshTopicHistoryChart();
                    });
                    layui.form.render('select');
                    refreshTopicSendChart();
                });

                form.on('select(consumerName)', function () {
                    refreshLagChart();
                });

                laydate.render({
                    elem: '#timeRange',
                    type: 'datetime',
                    range: true,
                    min: -${savingDays-1},
                    max: 1,
                    btns: ['confirm'],
                    done: function () {

                    }
                });

                $("#timeRange").change(function () {
                    refreshTopicSendChart();
                    refreshLagChart();
                    refreshConsumeTpsChart();
                });

                const n = new Date();
                n.setDate(n.getDate() + 1);
                const end = n.format('yyyy-MM-dd' + ' 00:00:00');
                n.setDate(n.getDate() - 1);
                n.setMinutes(n.getMinutes() - 30);
                const start = n.format('yyyy-MM-dd HH:mm' + ':00');
                $("#timeRange").val(start + ' - ' + end);

                form.on('checkbox(btnAutoRefresh)', function (data) {
                    autoRefresh(data.elem.checked);
                });

                $("#btnTopicRefresh").click(function () {
                    refreshTopicSendChart();
                    refreshLagChart();
                    refreshConsumeTpsChart();
                    refreshTopicRankChart();
                    refreshTopicHistoryChart();
                });

                let topicSendChart, lagChart = null, consumeTpsChart = null;

                function refreshTopicSendChart() {
                    const form = getForm();
                    if (form.topicName == null || form.topicName === '' || form.timeRange == null || form.timeRange === '') {
                        _init({topicNames: [], times: [], series: []});
                        return;
                    }
                    admin.post("getTopicSendChart?topicName=" + form.topicName + "&createTimeRange=" + form.timeRange, {}, function (data) {
                        _init(data.data);
                    });

                    function _init(data) {
                        const ele = $('#topicSendChart').children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        topicSendChart = echarts.init(ele[0], layui.echartsTheme);
                        topicSendChart.setOption(initTopicSendChart(data));
                        window.onresize = topicSendChart.resize;
                    }
                }

                function refreshLagChart() {
                    const form = getForm();
                    if (form.consumerId == null || form.consumerId === '' || form.timeRange == null || form.timeRange === '') {
                        _init({topicNames: [], times: [], series: []});
                        return;
                    }
                    admin.post("getLagChart?topicName=" + form.topicName + "&groupId=" + form.consumerId + "&createTimeRange=" + form.timeRange, {}, function (data) {
                        _init(data.data);
                    });

                    function _init(data) {
                        const ele = $('#lagChart').children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        lagChart = echarts.init(ele[0], layui.echartsTheme);
                        lagChart.setOption(initLagChart(data));
                        window.onresize = lagChart.resize;
                    }
                }

                function refreshConsumeTpsChart() {
                    const form = getForm();
                    if (form.consumerId == null || form.consumerId === '' || form.timeRange == null || form.timeRange === '') {
                        _init({topicNames: [], times: [], series: []});
                        return;
                    }

                    admin.post("getConsumeTpsChart?topicName=" + form.topicName + "&groupId=" + form.consumerId + "&createTimeRange=" + form.timeRange, {}, function (data) {
                        _init(data.data);
                    });

                    function _init(data) {
                        const ele = $('#consumeTpsChart').children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        consumeTpsChart = echarts.init(ele[0], layui.echartsTheme);
                        consumeTpsChart.setOption(initConsumeTpsChart(data));
                        window.onresize = consumeTpsChart.resize;
                    }
                }

                function refreshTopicHistoryChart() {
                    const form = getForm();
                    if (form.topicName == null || form.topicName === '') {
                        _init({topicNames: [], times: [], series: []});
                        return;
                    }
                    admin.post("getTopicHistoryChart?topicName=" + form.topicName, {}, function (data) {
                        _init(data.data);
                    });

                    function _init(data) {
                        const ele = $('#topicHistoryChart').children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        const echart = echarts.init(ele[0], layui.echartsTheme);
                        echart.setOption(initTopicHistoryChart(data));
                        window.onresize = echart.resize;
                    }
                }

                function refreshTopicRankChart() {
                    const form = getForm();
                    admin.post("getTopicRankChart?createTimeRange=" + form.timeRange, {}, function (data) {
                        _init(data.data);
                    });

                    function _init(data) {
                        const ele = $('#topicRankChart').children('div');
                        ele.removeAttr("_echarts_instance_").empty();
                        const echart = echarts.init(ele[0], layui.echartsTheme);
                        echart.setOption(initTopicRankChart(data));
                        window.onresize = echart.resize;
                    }
                }

                function getForm() {
                    const topicName = $.trim($("#topicName").siblings().find("dd[class='layui-this']").html());
                    const consumerId = $.trim($("#consumerName").siblings().find("dd[class='layui-this']").html());
                    const timeRange = $.trim($("#timeRange").val());
                    return {'topicName': topicName, 'consumerId': consumerId, 'timeRange': timeRange};
                }

                let timer = null;

                function autoRefresh(auto) {
                    if (timer != null) {
                        clearInterval(timer);
                    }
                    if (auto) {
                        timer = setInterval(function () {
                            const form = getForm();
                            const timeRange = checkTimeRange('timeRange', form.timeRange);
                            if (form.topicName != null && form.topicName !== '') {
                                admin.post("getTopicSendChart?topicName=" + form.topicName + "&createTimeRange=" + timeRange,
                                    {},
                                    function (data) {
                                        topicSendChart.setOption(initTopicSendChart(data.data));
                                    });

                                admin.post("getLagChart?topicName=" + form.topicName + "&groupId=" + form.consumerId + "&createTimeRange=" + timeRange,
                                    {},
                                    function (data) {
                                        lagChart.setOption(initLagChart(data.data));
                                    });

                                admin.post("getConsumeTpsChart?topicName=" + form.topicName + "&groupId=" + form.consumerId + "&createTimeRange=" + timeRange,
                                    {},
                                    function (data) {
                                        consumeTpsChart.setOption(initConsumeTpsChart(data.data));
                                    });
                            }
                        }, 30000);
                    }
                }

                function checkTimeRange(id, dateRange) {
                    const split = dateRange.split(' - ');
                    const start = new Date(split[0]);
                    const now = new Date();
                    if ((now - start) > 1000 * 60 * 40) {
                        now.setDate(now.getDate() + 1);
                        const to = now.format('yyyy-MM-dd' + ' 00:00:00');
                        now.setDate(now.getDate() - 1);
                        now.setMinutes(now.getMinutes() - 30);
                        const from = now.format('yyyy-MM-dd HH:mm' + ':00');
                        dateRange = from + ' - ' + to;
                        $("#" + id).val(dateRange);
                    }
                    return dateRange;
                }

                refreshTopicSendChart();
                refreshLagChart();
                refreshConsumeTpsChart();
                refreshTopicRankChart();
                refreshTopicHistoryChart();
                autoRefresh(true);
            });
        });
    </script>
    </body>
    </html>
</@compress>