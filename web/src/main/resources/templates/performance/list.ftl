<@compress>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-tab layui-tab-card">
            <ul class="layui-tab-title">
                <li class="layui-this">
                    <i class="layui-icon layui-icon-template-1" style="color: #1E9FFF;"></i>
                    ZooKeeper性能
                </li>
                <li>
                    <i class="layui-icon layui-icon-app" style="color: #1E9FFF;"></i>
                    Kafka性能
                </li>
                <li>
                    <i class="layui-icon layui-icon-tabs" style="color: #1E9FFF;"></i>
                    Kafka指标
                </li>
            </ul>
            <div class="layui-tab-content">
                <div class="layui-tab-item layui-show">
                    1
                </div>
                <div class="layui-tab-item">2</div>
                <div class="layui-tab-item">3</div>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {

        });
    </script>
    </body>
    </html>
</@compress>