<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "common/layui.ftl">
    </head>
    <body class="layui-layout-body">
    <div id="LAY_app">
        <div class="layui-layout layui-layout-admin">
            <div class="layui-header">
                <ul class="layui-nav layui-layout-left">
                    <li class="layui-nav-item layadmin-flexible" lay-unselect>
                        <a href="javascript:" layadmin-event="flexible" title="侧边伸缩">
                            <i class="layui-icon layui-icon-shrink-right" id="LAY_app_flexible"></i>
                        </a>
                    </li>
                    <li class="layui-nav-item" lay-unselect>
                        <a href="javascript:" layadmin-event="refresh" title="刷新">
                            <i class="layui-icon layui-icon-refresh-3"></i>
                        </a>
                    </li>
                </ul>
                <ul class="layui-nav layui-layout-right" lay-filter="layadmin-layout-right">
                    <li class="layui-nav-item layui-hide-xs" lay-unselect>
                        <a href="javascript:" layadmin-event="fullscreen">
                            <i class="layui-icon layui-icon-screen-full"></i>
                        </a>
                    </li>
                    <li class="layui-nav-item" style="margin-right: 10px" lay-unselect>
                        <a href="javascript:">
                            <cite>Administrator</cite>
                        </a>
                        <dl class="layui-nav-child">
                            <dd><a lay-href="set/user/info.html">基本资料</a></dd>
                            <dd><a lay-href="set/user/password.html">修改密码</a></dd>
                            <hr>
                            <dd layadmin-event="logout" style="text-align: center;"><a>退出</a></dd>
                        </dl>
                    </li>
                </ul>
            </div>
            <div class="layui-side layui-side-menu">
                <div class="layui-side-scroll">
                    <div class="layui-logo" lay-href="${ctx}/dashboard/index">
                        <span>Kafka管理监控平台</span>
                    </div>
                    <ul class="layui-nav layui-nav-tree" lay-shrink="all" id="LAY-system-side-menu"
                        lay-filter="layadmin-system-side-menu">
                        <li data-name="dashboard" class="layui-nav-item">
                            <a href="javascript:" lay-href="${ctx}/dashboard/index" lay-tips="仪表盘" lay-direction="2">
                                <i class="layui-icon layui-icon-console"></i>
                                <cite>仪表盘</cite>
                            </a>
                        </li>
                        <li data-name="dashboard" class="layui-nav-item">
                            <a href="javascript:" lay-href="${ctx}/cluster/tolist" lay-tips="主题" lay-direction="2">
                                <i class="layui-icon layui-icon-share"></i>
                                <cite>集群</cite>
                            </a>
                        </li>
                        <li data-name="dashboard" class="layui-nav-item">
                            <a href="javascript:" lay-href="${ctx}/topic/tolist" lay-tips="主题" lay-direction="2">
                                <i class="layui-icon layui-icon-dialogue"></i>
                                <cite>主题</cite>
                            </a>
                        </li>
                        <li data-name="consumer" class="layui-nav-item">
                            <a href="javascript:" lay-href="${ctx}/consumer/tolist" lay-tips="消费者" lay-direction="2">
                                <i class="layui-icon layui-icon-group"></i>
                                <cite>消费者</cite>
                            </a>
                        </li>
                        <li data-name="consumer" class="layui-nav-item">
                            <a href="javascript:" lay-href="${ctx}/record/tolist" lay-tips="消息跟踪" lay-direction="2">
                                <i class="layui-icon layui-icon-list"></i>
                                <cite>消息跟踪</cite>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="layadmin-pagetabs" id="LAY_app_tabs">
                <div class="layui-icon layadmin-tabs-control layui-icon-prev" layadmin-event="leftPage"></div>
                <div class="layui-icon layadmin-tabs-control layui-icon-next" layadmin-event="rightPage"></div>
                <div class="layui-icon layadmin-tabs-control layui-icon-down">
                    <ul class="layui-nav layadmin-tabs-select" lay-filter="layadmin-pagetabs-nav">
                        <li class="layui-nav-item" lay-unselect>
                            <a href="javascript:"></a>
                            <dl class="layui-nav-child layui-anim-fadein">
                                <dd layadmin-event="closeThisTabs"><a href="javascript:">关闭当前标签页</a></dd>
                                <dd layadmin-event="closeOtherTabs"><a href="javascript:">关闭其它标签页</a></dd>
                                <dd layadmin-event="closeAllTabs"><a href="javascript:">关闭全部标签页</a></dd>
                            </dl>
                        </li>
                    </ul>
                </div>
                <div class="layui-tab" lay-unauto lay-allowClose="true" lay-filter="layadmin-layout-tabs">
                    <ul class="layui-tab-title" id="LAY_app_tabsheader">
                        <li lay-id="${ctx}/dashboard/index" lay-attr="${ctx}/dashboard/index" class="layui-this"><i
                                    class="layui-icon layui-icon-home"></i></li>
                    </ul>
                </div>
            </div>
            <div class="layui-body" id="LAY_app_body">
                <div class="layadmin-tabsbody-item layui-show">
                    <iframe src="${ctx}/dashboard/index" style="border:0" class="layadmin-iframe"></iframe>
                </div>
            </div>
            <div class="layadmin-body-shade" layadmin-event="shade"></div>
        </div>
    </div>
    <script>
        layui.config({base: '..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use('index', function () {
        });
    </script>
    </body>
    </html>
</@compress>