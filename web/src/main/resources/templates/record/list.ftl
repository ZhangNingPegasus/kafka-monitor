<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-card">

            <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                <div class="layui-form-item">
                    <div class="layui-inline">主题名称</div>
                    <div class="layui-inline" style="width:300px">
                        <select name="topicName" lay-filter="topicName" lay-verify="required" lay-search>
                            <option value="">请选择主题</option>
                            <#list topics as topic >
                                <option value="${topic.topicName}">${topic.topicName}</option>
                            </#list>
                        </select>
                    </div>

                    <div class="layui-inline">分区号</div>
                    <div class="layui-inline" style="width:200px">
                        <select name="partitionId" lay-verify="required" lay-search>
                            <option value="">所有分区</option>
                        </select>
                    </div>

                    <div class="layui-inline">消息Key</div>
                    <div class="layui-inline" style="width:300px">
                        <input type="text" name="key" placeholder="请输入消息Key" autocomplete="off" class="layui-input">
                    </div>

                    <div class="layui-inline">时间范围</div>
                    <div class="layui-inline" style="width:300px">
                        <input type="text" id="createTimeRange" name="createTimeRange" lay-verify="required"
                               class="layui-input" placeholder="请选择时间范围">
                    </div>

                    <div class="layui-inline">
                        <button class="layui-btn layuiadmin-btn-admin" lay-submit lay-filter="search">
                            <i class="layui-icon layui-icon-search layuiadmin-button-btn"></i>
                        </button>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>
                <script type="text/html" id="grid-bar">
                    <@update>
                        <a class="layui-btn layui-btn-xs" lay-event="sendMsg"><i
                                    class="layui-icon layui-icon-dialogue"></i>重发消息</a>
                    </@update>
                    <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="showMsgDetails"><i
                                class="layui-icon layui-icon-login-wechat" style="color:white"></i>消息详情</a>
                    <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="showConsumerDetails"><i
                                class="layui-icon layui-icon-user" style="color:white"></i>消费详情</a>
                </script>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'laydate'], function () {
            const admin = layui.admin, laydate = layui.laydate, $ = layui.$, form = layui.form, table = layui.table;
            let topicName = '';
            laydate.render({elem: '#createTimeRange', type: 'datetime', range: true});

            const now = new Date();
            now.setDate(now.getDate() + 1);
            const to = now.format('yyyy-MM-dd' + ' 00:00:00');
            now.setDate(now.getDate() - 1);
            now.setMinutes(now.getMinutes() - 15);
            const from = now.format('yyyy-MM-dd HH:mm:ss');
            $("#createTimeRange").val(from + ' - ' + to);

            form.on('select(topicName)', function (data) {
                topicName = data.value;
                $("select[name=partitionId]").html("<option value=\"-1\">所有分区</option>");
                form.render('select');
                if ($.trim(data.value) === '') {
                    return;
                }
                admin.post('listTopicPartitions', {'topicName': data.value}, function (res) {
                    $.each(res.data, function (key, val) {
                        const option = $("<option>").val(val.partitionId).text(val.partitionId);
                        $("select[name=partitionId]").append(option);
                        form.render('select');
                    });
                    $("select[name=partitionId]").get(0).selectedIndex = 0;
                });
            });

            form.on('submit(search)', function (data) {
                const field = data.field;
                table.reload('grid', {where: field, page: 1});
            });

            table.render({
                elem: '#grid',
                url: 'list',
                toolbar: '#grid-toolbar',
                method: 'post',
                cellMinWidth: 80,
                page: true,
                limit: 15,
                limits: [15],
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {type: 'numbers', title: '序号', width: 80},
                    {field: 'partitionId', title: '分区号', width: 120},
                    {field: 'offset', title: '偏移量', width: 120},
                    {field: 'key', title: '消息Key', width: 220},
                    {field: 'createTime', title: '消息时间', width: 180},
                    {field: 'value', title: '消息体'},
                    {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 285}
                ]]
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'sendMsg') {
                    layer.confirm('确定要重发消息吗?', function (index) {
                        admin.post('resend', data, function () {
                            layer.close(index);
                            layer.msg('发送成功');
                        }, function (result) {
                            admin.error(admin.OPT_FAILURE, result.error);
                        });
                    });
                } else if (obj.event === 'showMsgDetails') {
                    if (!data.key) {
                        data.key = "";
                    }
                    data.topicName = topicName;
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-login-wechat" style="color: #1E9FFF;"></i>&nbsp;消息详情',
                        content: 'tomsgdetail?topicName=' + data.topicName + '&partitionId=' + data.partitionId + '&offset=' + data.offset + '&key=' + data.key + '&createTime=' + data.createTime,
                        shadeClose: true,
                        shade: 0.8,
                        area: ['880px', '820px']
                    });
                } else if (obj.event === 'showConsumerDetails') {
                    data.topicName = topicName;
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-user" style="color: #1E9FFF;"></i>&nbsp;消费详情',
                        content: 'toconsumerdetail?topicName=' + data.topicName + '&partitionId=' + data.partitionId + '&offset=' + data.offset,
                        shadeClose: true,
                        shade: 0.8,
                        area: ['880px', '820px']
                    });
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>