<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
         style="padding: 20px 30px 0 0;">

        <div class="layui-form-item">
            <label class="layui-form-label">主题名称</label>
            <div class="layui-input-inline" style="width:700px">
                <select name="topicName" lay-filter="topicName" autofocus="autofocus"
                        lay-verify="required" lay-search>
                    <option value="">请选择主题名称</option>
                    <#list topics as topic>
                        <option value="${topic}">${topic}</option>
                    </#list>
                </select>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">监控时间</label>
            <div class="layui-input-inline" style="width:700px">
                <input id="rangeTime" name="rangeTime" type="text" class="layui-input" placeholder=" - "
                       autocomplete="off"/>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">TPS下限</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="number" name="fromTps" placeholder="请填写TPS下限" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">TPS上限</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="number" name="toTps" placeholder="请填写TPS上限" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">变化下限</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="number" name="fromMomTps" placeholder="请填写TPS变化下限" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">变化上限</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="number" name="toMomTps" placeholder="请填写TPS变化上限" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">通知邮箱</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="email" name="email" placeholder="请填写警告接受邮箱地址" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form', 'laydate'], function () {
            const laydate = layui.laydate;
            parent.layer.iframeAuto(parent.layer.getFrameIndex(window.name));

            laydate.render({
                elem: '#rangeTime',
                type: 'time',
                range: true
            });
        });
    </script>
    </body>
    </html>
</@compress>