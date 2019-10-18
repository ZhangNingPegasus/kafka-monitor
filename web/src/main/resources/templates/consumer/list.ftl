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

            <script type="text/html" id="topicCount">
                {{#  if(d.topicCount > 0){ }}
                <span class="layui-badge layui-bg-green">{{ d.topicCount }}</span>
                {{#  } else { }}
                <span class="layui-badge layui-bg-orange">{{ d.topicCount }}</span>
                {{#  } }}
            </script>

            <script type="text/html" id="activeTopicCount">
                {{#  if(d.activeTopicCount > 0){ }}
                <span class="layui-badge layui-bg-green">{{ d.activeTopicCount }}</span>
                {{#  } else { }}
                <span class="layui-badge layui-bg-orange">{{ d.activeTopicCount }}</span>
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

<script>
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
        var admin = layui.admin, table = layui.table, $ = layui.$, carousel = layui.carousel, echarts = layui.echarts;
        //区块轮播切换
        layui.use(['carousel'], function () {
            var $ = layui.$
                , carousel = layui.carousel
                , element = layui.element
                , device = layui.device();

            //轮播切换
            $('.layadmin-carousel').each(function () {
                var othis = $(this);
                carousel.render({
                    elem: this
                    , width: '100%'
                    , arrow: 'none'
                    , interval: othis.data('interval')
                    , autoplay: othis.data('autoplay') === true
                    , trigger: (device.ios || device.android) ? 'click' : 'hover'
                    , anim: othis.data('anim')
                });
            });
            element.render('progress');
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
                {type: 'numbers', title: '序号', width: 50},
                {field: 'groupId', title: '组名称', width: 500},
                {field: 'node', title: '节点', width: 300},
                {title: '订阅主题数', sort: true, templet: "#topicCount", width: 150},
                {title: '活跃主题', sort: true, templet: "#activeTopicCount", width: 150},
                {fixed: 'right', title: '操作', toolbar: '#grid-bar'}
            ]],
            done: function () {
                admin.post("getChartData", {}, function (data) {
                    var echnormline = [], elemnormline = $('#activeTopics').children('div'),
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
                            window.onresize = echnormline[index].resize;
                        };
                    if (!elemnormline[0]) return;
                    rendernormline(0);
                });
            }
        });

        table.on('tool(grid)', function (obj) {
            var data = obj.data;
            if (obj.event === 'del') {
                layer.confirm(admin.DEL_QUESTION, function (index) {
                    admin.post("del", {consumerGroupId: data.groupId}, function () {
                        table.reload('grid');
                        layer.close(index);
                    });
                });
            } else if (obj.event === 'getDetail') {
                layer.open({
                    type: 2,
                    title: '消费组详细信息',
                    shadeClose: true,
                    shade: 0.8,
                    area: ['90%', '90%'],
                    content: 'todetail/' + data.groupId
                });
            }
        });
    });
</script>

</body>
</html>