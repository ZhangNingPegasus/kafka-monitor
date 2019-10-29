<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
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
                            <select id="topicName" name="topicName" lay-filter="topicName"
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
                                   class="layui-input" placeholder="请选择创建时间范围" autocomplete="off">
                        </div>
                        <button id="btnTopicRefresh" type="button" class="layui-btn layui-btn-xs">刷 新</button>
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
                <div class="layui-card-header">主题前5排行榜
                    <button id="btnTopicRankRefresh" type="button" class="layui-btn layui-btn-xs"
                            style="float: right;margin-top: 10px">刷 新
                    </button>
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
                            <input type="text" id="lagCreateTimeRange" name="lagCreateTimeRange" lay-verify="required"
                                   class="layui-input" placeholder="请选择创建时间范围" autocomplete="off">
                        </div>
                        <button id="btnLagRefresh" type="button" class="layui-btn layui-btn-xs">刷 新</button>
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
                <div class="layui-card-header">暂定</div>
                <div class="layui-card-body">

                    <div class="layui-carousel layadmin-carousel layadmin-dataview" data-anim="fade"
                         lay-filter="LAY-index-heapbar">
                        <div carousel-item id="LAY-index-heapbar">
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
        const admin = layui.admin, form = layui.form, laydate = layui.laydate, $ = layui.$, echarts = layui.echarts;
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

            function refreshLagChart() {
                const consumerId = $.trim($("#consumerName").siblings().find("dd[class='layui-this']").html());
                const lagCreateTimeRange = $.trim($("#lagCreateTimeRange").val());
                if (consumerId == null || consumerId === '' || lagCreateTimeRange == null || lagCreateTimeRange === '') {
                    return;
                }
                $('#lagChart').children('div').removeAttr("_echarts_instance_").empty();
                admin.post("getLagChart?groupId=" + consumerId + "&createTimeRange=" + lagCreateTimeRange, {}, function (data) {
                    data = data.data;
                    const echnormline = [], elemnormline = $('#lagChart').children('div'),
                        rendernormline = function (index) {
                            echnormline[index] = echarts.init(elemnormline[index], layui.echartsTheme);
                            echnormline[index].setOption({
                                title: {
                                    text: '消息堆积图'
                                },
                                grid: {
                                    left: '4%',
                                    right: '9%',
                                    bottom: 35,
                                    containLabel: true
                                },
                                toolbox: {
                                    feature: {
                                        dataZoom: {
                                            yAxisIndex: 'none'
                                        },
                                        restore: {},
                                        saveAsImage: {}
                                    }
                                },
                                tooltip: {
                                    trigger: 'axis',
                                    axisPointer: {
                                        type: 'cross',
                                        animation: false,
                                        label: {
                                            backgroundColor: '#505765'
                                        }
                                    }
                                },
                                dataZoom: [
                                    {
                                        show: true,
                                        realtime: true,
                                        start: 0,
                                        end: 100
                                    },
                                    {
                                        type: 'inside',
                                        realtime: true,
                                        start: 0,
                                        end: 100
                                    }
                                ],
                                yAxis: {
                                    type: 'value'
                                },
                                legend: {
                                    data: data.topicNames,
                                    x: 'center'
                                },
                                xAxis: {
                                    type: 'category',
                                    boundaryGap: false,
                                    axisLine: {onZero: false},
                                    data: data.times.map(function (str) {
                                        return str.replace(' ', '\n')
                                    })
                                },
                                series: data.series
                            });
                            window.onresize = echnormline[index].resize;
                        };
                    if (!elemnormline[0]) return;
                    rendernormline(0);
                });
            }

            function refreshTopicChart() {
                const topicName = $.trim($("#topicName").siblings().find("dd[class='layui-this']").html());
                const topicCreateTimeRange = $.trim($("#topicCreateTimeRange").val());
                if (topicName == null || topicName === '' || topicCreateTimeRange == null || topicCreateTimeRange === '') {
                    return;
                }
                $('#topicChart').children('div').removeAttr("_echarts_instance_").empty();
                admin.post("getTopicChart?topicName=" + topicName + "&createTimeRange=" + topicCreateTimeRange, {}, function (data) {
                    data = data.data;
                    const echnormline = [], elemnormline = $('#topicChart').children('div'),
                        rendernormline = function (index) {
                            echnormline[index] = echarts.init(elemnormline[index], layui.echartsTheme);
                            echnormline[index].setOption({
                                title: {
                                    text: '消息产生总量图'
                                },
                                grid: {
                                    left: '4%',
                                    right: '9%',
                                    bottom: 35,
                                    containLabel: true
                                },
                                toolbox: {
                                    feature: {
                                        dataZoom: {
                                            yAxisIndex: 'none'
                                        },
                                        restore: {},
                                        saveAsImage: {}
                                    }
                                },
                                tooltip: {
                                    trigger: 'axis',
                                    axisPointer: {
                                        type: 'cross',
                                        animation: false,
                                        label: {
                                            backgroundColor: '#505765'
                                        }
                                    }
                                },
                                dataZoom: [
                                    {
                                        show: true,
                                        realtime: true,
                                        start: 0,
                                        end: 100
                                    },
                                    {
                                        type: 'inside',
                                        realtime: true,
                                        start: 0,
                                        end: 100
                                    }
                                ],
                                yAxis: {
                                    type: 'value'
                                },
                                legend: {
                                    data: data.topicNames,
                                    x: 'center'
                                },
                                xAxis: {
                                    type: 'category',
                                    boundaryGap: false,
                                    axisLine: {onZero: false},
                                    data: data.times.map(function (str) {
                                        return str.replace(' ', '\n')
                                    })
                                },
                                series: data.series
                            });
                            window.onresize = echnormline[index].resize;
                        };
                    if (!elemnormline[0]) return;
                    rendernormline(0);
                });
            }

            function refreshTopicRankChart() {
                $('#topicRankChart').children('div').removeAttr("_echarts_instance_").empty();
                admin.post("getTopicRankChart", {}, function (data) {
                    data = data.data;
                    const echnormline = [], elemnormline = $('#topicRankChart').children('div'),
                        rendernormline = function (index) {
                            echnormline[index] = echarts.init(elemnormline[index], layui.echartsTheme);
                            echnormline[index].setOption({
                                grid: {
                                    left: '4%',
                                    right: '9%',
                                    bottom: 35,
                                    containLabel: true
                                },
                                toolbox: {
                                    feature: {
                                        dataZoom: {
                                            yAxisIndex: 'none'
                                        },
                                        restore: {},
                                        saveAsImage: {}
                                    }
                                },
                                tooltip: {
                                    trigger: 'axis',
                                    axisPointer: {
                                        type: 'cross',
                                        animation: false,
                                        label: {
                                            backgroundColor: '#505765'
                                        }
                                    }
                                },
                                xAxis: {
                                    type: 'category',
                                    nameTextStyle: {fontSize: 2},
                                    data: data.topicNames.map(function (str) {
                                        if (str.length > 15) {
                                            return str.substr(0, 15) + '...'
                                        } else {
                                            return str;
                                        }
                                    })
                                },
                                yAxis: {
                                    type: 'value'
                                },
                                series: data.series
                            });
                            window.onresize = echnormline[index].resize;
                        };
                    if (!elemnormline[0]) return;
                    rendernormline(0);
                });
            }

            laydate.render({
                elem: '#lagCreateTimeRange',
                type: 'datetime',
                range: true,
                done: function () {
                    refreshLagChart();
                }
            });

            laydate.render({
                elem: '#topicCreateTimeRange',
                type: 'datetime',
                range: true,
                done: function () {
                    refreshTopicChart();
                }
            });

            const now = new Date();
            now.setDate(now.getDate() + 1);
            const to = now.format('yyyy-MM-dd' + ' 00:00:00');
            now.setDate(now.getDate() - 1);
            now.setMinutes(now.getMinutes() - 60);
            const from = now.format('yyyy-MM-dd HH:mm' + ':00');
            $("#lagCreateTimeRange").val(from + ' - ' + to);
            $("#topicCreateTimeRange").val(from + ' - ' + to);


            form.on('select(consumerName)', function () {
                refreshLagChart();
            });

            $("#lagCreateTimeRange").change(function () {
                refreshLagChart();
            });

            $("#btnLagRefresh").click(function () {
                refreshLagChart();
            });

            form.on('select(topicName)', function () {
                refreshTopicChart();
            });

            $("#topicCreateTimeRange").change(function () {
                refreshTopicChart();
            });

            $("#btnTopicRefresh").click(function () {
                refreshTopicChart();
            });

            $("#btnTopicRankRefresh").click(function () {
                refreshTopicRankChart();
            });


            $('#consumerName option:eq(1)').attr('selected', 'selected');
            layui.form.render('select');

            refreshTopicChart();
            refreshLagChart();
            refreshTopicRankChart();
        });
    });
</script>


</body>
</html>