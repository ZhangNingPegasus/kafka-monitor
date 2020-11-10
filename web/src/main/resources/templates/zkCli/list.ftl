<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <link rel="stylesheet" href="${ctx}/terminal/jquery.terminal-2.8.0.min.css" media="all">
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-card">
            <div class="layui-card-body">
                <div id="zkcli" class="terminal" style="height: 800px">
                </div>
            </div>
        </div>
    </div>

    <script src="${ctx}/js/jquery-3.4.1.min.js"></script>
    <script src="${ctx}/terminal/jquery.terminal-2.8.0.min.js"></script>
    <script src="${ctx}/terminal/keyboard.js"></script>
    <script type="text/javascript">
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const height = 800, admin = layui.admin;

            function execute(command, term) {
                let type = '';
                if (command.indexOf("ls") > -1) {
                    type = 'ls';
                } else if (command.indexOf("get") > -1) {
                    type = 'get';
                } else {
                    throw new Error("目前仅支持: ls、get命令，例如: ls / ");
                }

                if (type !== '') {
                    admin.post('execute', {'command': command, 'type': type}, function (res) {
                        term.echo(String(res.data));
                    }, function (res) {
                        term.error(res.error);
                    });
                }
            }

            admin.post('zkInfo', {}, function (data) {
                $("#zkcli").terminal(function (command, term) {
                    if (command !== '') {
                        try {
                            execute(command, term);
                        } catch (e) {
                            term.error(String(e));
                        }
                    } else {
                        term.echo('');
                    }
                }, {
                    greetings: '********************************************************************************\n' + 'Name &nbsp;:  Zookeeper 客户端\n' + 'Server :  ' + data.data + '\n' + '********************************************************************************\n',
                    height: height,
                    prompt: '[zk: (CONNECTED) ] > '
                });
            }, function (data) {
                $('#zkcli').terminal(function (command, term) {
                }, {
                    greetings: '********************************************************************************\n' + 'Name &nbsp;:  Zookeeper 客户端\n' + 'Server :  [' + data.data + ']\n' + '********************************************************************************\n',
                    height: height,
                    prompt: '[zk: (DISCONNECTED) ] > '
                });
            });
        });
    </script>
    </body>
    </html>
</@compress>