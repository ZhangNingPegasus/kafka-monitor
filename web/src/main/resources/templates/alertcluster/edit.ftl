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
            <label class="layui-form-label">集群类型</label>
            <div class="layui-input-inline" style="width:700px">
                <select name="type" lay-filter="type" autofocus="autofocus"
                        lay-verify="required" lay-search>
                    <option value="">请选择集群类型</option>
                    <#list type as t>
                        <option value="${t.code}"
                                selected="${(item.type==t.code)?string('selected','')}">${t}</option>
                    </#list>
                </select>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">主机地址</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="text" name="server" lay-verify="required" placeholder="请填写集群中的主机地址,如:192.168.6.166:2181"
                       autocomplete="off"
                       class="layui-input" value="${item.server}">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">通知邮箱</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="email" name="email" placeholder="请填写警告接受邮箱地址" autocomplete="off"
                       class="layui-input" value="${item.email}">
            </div>
        </div>


        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form'], function () {
            parent.layer.iframeAuto(parent.layer.getFrameIndex(window.name));
        });
    </script>
    </body>
    </html>
</@compress>