<@compress>
    <#assign ctx=request.contextPath>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <meta charset="utf-8">
        <title>Kafka管理监控平台</title>
        <script src="${ctx}/js/jquery.js"></script>
        <script src="${ctx}/layuiadmin/lib/extend/echarts.js"></script>
        <script type="text/javascript" src="${ctx}/js/jquery.liMarquee.js"></script>
        <script type="text/javascript" src="${ctx}/js/jquery.cxselect.min.js"></script>
        <script type="text/javascript" src="${ctx}/js/utils.js"></script>
        <link rel="stylesheet" href="${ctx}/css/comon0.css">
    </head>
    <body style="overflow: hidden">
    <div style="background-color: #010b46;background-image: radial-gradient(#012ae6,#030d4a)">
        <div class="loading" style="display: none;">
            <div class="loadbox"><img src="${ctx}/images/loading.gif" alt="页面加载中">页面加载中...</div>
        </div>
        <div class="back"></div>
        <div class="head">
            <div class="weather"><span id="showTime"></span></div>
            <h1 id="toggleScreen" onclick="toggleScreen()" style="cursor:pointer" title="点击全屏">Kafka管理监控分析统计可视化云平台</h1>
        </div>
        <script type="text/javascript">
            let isFullScreen = false;

            function fullScreen() {
                if (isFullScreen) {
                    return;
                }
                isFullScreen = true;
                $("#toggleScreen").attr("title", "点击退出全屏");
                const el = document.documentElement;
                const rfs = el.requestFullScreen || el.webkitRequestFullScreen;
                if (typeof rfs != "undefined" && rfs) {
                    rfs.call(el);
                } else if (typeof window.ActiveXObject != "undefined") {
                    const wscript = new ActiveXObject("WScript.Shell");
                    if (wscript != null) {
                        wscript.SendKeys("{F11}");
                    }
                } else if (el.msRequestFullscreen) {
                    el.msRequestFullscreen();
                } else if (el.oRequestFullscreen) {
                    el.oRequestFullscreen();
                }
            }

            function exitFullScreen() {
                if (!isFullScreen) {
                    return;
                }
                isFullScreen = false;
                $("#toggleScreen").attr("title", "点击全屏");
                const el = document;
                const cfs = el.cancelFullScreen || el.webkitCancelFullScreen ||
                    el.mozCancelFullScreen || el.exitFullScreen;
                if (typeof cfs != "undefined" && cfs) {
                    cfs.call(el);
                } else if (typeof window.ActiveXObject != "undefined") {
                    const wscript = new ActiveXObject("WScript.Shell");
                    if (wscript != null) {
                        wscript.SendKeys("{F11}");
                    }
                }
            }

            function toggleScreen() {
                if (isFullScreen) {
                    exitFullScreen();
                } else {
                    fullScreen();
                }
            }

            $(window).load(function () {
                $(".loading").fadeOut()
            });
            $("#showTime").html(new Date().format("yyyy年MM月dd日 HH时mm分ss秒"));
            setInterval(function () {
                $("#showTime").html(new Date().format("yyyy年MM月dd日 HH时mm分ss秒"));
            }, 1000);

        </script>
        <div class="mainbox">
            <ul class="clearfix">
                <li>
                    <div class="boxall" style="height:950px;">
                        <div class="alltitle">主题消息生产总量</div>
                        <div class="navboxall">
                            <table class="table1" width="100%" border="0" cellspacing="0" cellpadding="0">
                                <tbody>
                                <tr>
                                    <th scope="col">排名</th>
                                    <th scope="col">主题</th>
                                    <th scope="col">数量</th>
                                    <th scope="col">增长率</th>
                                </tr>
                                <#list topicRecordCountVoList as item>
                                    <tr>
                                        <td><span>${item_index+1}</span></td>
                                        <td>${item.topicName}</td>
                                        <td>${item.logSize}<br></td>
                                        <td>
                                            <#if item.growthRate??>
                                                ${item.growthRate * 100 + '%'}
                                            <#else>
                                                NA
                                            </#if>
                                            <br>
                                        </td>
                                    </tr>
                                </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </li>
                <li>
                    <div class="boxall" style="height:230px">
                        <div class="clearfix navboxall" style="height: 100%">
                            <div class="pulll_left num">
                                <div class="numbt">${savingDays}天消息总数</div>
                                <div class="numtxt">${totalRecordCount}</div>
                            </div>
                            <div class="pulll_right zhibiao">
                                <div class="zkCount"><span>ZooKeeper总数</span>
                                    <div id="zkCount"
                                         style="-webkit-tap-highlight-color: transparent; user-select: none;">
                                        <div style="position: relative; overflow: hidden; width: 155px; height: 170px; padding: 0; margin: 0; border-width: 0; cursor: default;">
                                            <canvas data-zr-dom-id="zr_0" width="155" height="170"
                                                    style="position: absolute; left: 0; top: 0; width: 155px; height: 170px; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); padding: 0; margin: 0; border-width: 0;"></canvas>
                                        </div>
                                    </div>
                                </div>
                                <div class="kafkaCount"><span>Kafka总数</span>
                                    <div id="kafkaCount"
                                         style="-webkit-tap-highlight-color: transparent; user-select: none;">
                                        <div style="position: relative; overflow: hidden; width: 155px; height: 170px; padding: 0; margin: 0; border-width: 0; cursor: default;">
                                            <canvas data-zr-dom-id="zr_0" width="155" height="170"
                                                    style="position: absolute; left: 0; top: 0; width: 155px; height: 170px; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); padding: 0; margin: 0; border-width: 0;"></canvas>
                                        </div>
                                    </div>
                                </div>
                                <div class="topicCount"><span>Topic总数</span>
                                    <div id="topicCount"
                                         style="-webkit-tap-highlight-color: transparent; user-select: none;">
                                        <div style="position: relative; overflow: hidden; width: 155px; height: 170px; padding: 0; margin: 0; border-width: 0; cursor: default;">
                                            <canvas data-zr-dom-id="zr_0" width="155" height="170"
                                                    style="position: absolute; left: 0; top: 0; width: 155px; height: 170px; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); padding: 0; margin: 0; border-width: 0;"></canvas>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="boxall" style="height:350px">
                        <div class="alltitle">CPU使用率</div>
                        <div class="navboxall" id="cpuChart"
                             style="-webkit-tap-highlight-color: transparent; user-select: none; position: relative;">
                            <div style="position: relative; overflow: hidden; width: 933px; height: 280px; padding: 0; margin: 0; border-width: 0;">
                                <canvas data-zr-dom-id="zr_0" width="933" height="280"
                                        style="position: absolute; left: 0; top: 0; width: 933px; height: 280px; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); padding: 0; margin: 0; border-width: 0;"></canvas>
                            </div>
                            <div></div>
                        </div>
                    </div>
                    <div class="boxall" style="height:340px">
                        <div class="alltitle">线程数</div>
                        <div class="navboxall" id="threadChart"
                             style="-webkit-tap-highlight-color: transparent; user-select: none; position: relative;">
                            <div style="position: relative; overflow: hidden; width: 933px; height: 290px; padding: 0; margin: 0; border-width: 0; cursor: default;">
                                <canvas data-zr-dom-id="zr_0" width="933" height="290"
                                        style="position: absolute; left: 0; top: 0; width: 933px; height: 290px; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); padding: 0; margin: 0; border-width: 0;"></canvas>
                            </div>
                        </div>
                    </div>
                </li>
                <li>
                    <div class="boxall" style="height:950px">
                        <div class="alltitle">消费组消息堆积量</div>
                        <div class="navboxall" id="consumerChart"
                             style="-webkit-tap-highlight-color: transparent; user-select: none; position: relative;">
                            <div style="position: relative; overflow: hidden; width: 407px; height: 340px; padding: 0; margin: 0; border-width: 0;">
                                <canvas data-zr-dom-id="zr_0" width="407" height="340"
                                        style="position: absolute; left: 0; top: 0; width: 407px; height: 340px; user-select: none; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); padding: 0; margin: 0; border-width: 0;"></canvas>
                            </div>
                            <div></div>
                        </div>
                    </div>
                </li>
            </ul>
        </div>
    </div>

    <script type="text/javascript">
        $(function () {
            cpuChart();
            threadChart();
            consumerChart();
            zkCount();
            kafkaCount();
            topicCount();

            function cpuChart() {
                const myChart = echarts.init(document.getElementById('cpuChart'));
                myChart.setOption({
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {
                            lineStyle: {
                                color: '#57617B'
                            }
                        }
                    },
                    legend: {
                        data: ['系统CPU使用率', 'Kafka CPU使用率'],
                        top: '0',
                        textStyle: {
                            color: "#fff"
                        },
                        itemGap: 20,
                    },
                    grid: {
                        left: '0',
                        right: '20',
                        top: '10',
                        bottom: '20',
                        containLabel: true
                    },
                    xAxis: [{
                        type: 'category',
                        boundaryGap: false,
                        axisLabel: {
                            show: true,
                            textStyle: {
                                color: 'rgba(255,255,255,.6)'
                            }
                        },
                        axisLine: {
                            lineStyle: {
                                color: 'rgba(255,255,255,.1)'
                            }
                        },
                        data: ${cpuInfo.strXAxis}
                    }, {}],
                    yAxis: [{
                        axisLabel: {
                            show: true,
                            formatter: '{value}%',
                            textStyle: {
                                color: 'rgba(255,255,255,.6)'
                            }
                        },
                        axisLine: {
                            lineStyle: {
                                color: 'rgba(255,255,255,.1)'
                            }
                        },
                        splitLine: {
                            lineStyle: {
                                color: 'rgba(255,255,255,.1)'
                            }
                        }
                    }],
                    series: [{
                        name: '系统CPU使用率',
                        type: 'line',
                        smooth: true,
                        symbol: 'circle',
                        symbolSize: 5,
                        showSymbol: false,
                        lineStyle: {
                            normal: {
                                width: 2
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                                    offset: 0,
                                    color: 'rgba(24, 163, 64, 0.3)'
                                }, {
                                    offset: 0.8,
                                    color: 'rgba(24, 163, 64, 0)'
                                }], false),
                                shadowColor: 'rgba(0, 0, 0, 0.1)',
                                shadowBlur: 10
                            }
                        },
                        itemStyle: {
                            normal: {
                                color: '#cdba00',
                                borderColor: 'rgba(137,189,2,0.27)',
                                borderWidth: 12
                            }
                        },
                        data: ${cpuInfo.strSystemCpu}
                    }, {
                        name: 'Kafka CPU使用率',
                        type: 'line',
                        smooth: true,
                        symbol: 'circle',
                        symbolSize: 5,
                        showSymbol: false,
                        lineStyle: {
                            normal: {
                                width: 2
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                                    offset: 0,
                                    color: 'rgba(39, 122,206, 0.3)'
                                }, {
                                    offset: 0.8,
                                    color: 'rgba(39, 122,206, 0)'
                                }], false),
                                shadowColor: 'rgba(0, 0, 0, 0.1)',
                                shadowBlur: 10
                            }
                        },
                        itemStyle: {
                            normal: {
                                color: '#277ace',
                                borderColor: 'rgba(0,136,212,0.2)',
                                borderWidth: 12
                            }
                        },
                        data: ${cpuInfo.strProcessCpu}
                    }]
                });
                window.addEventListener("resize", function () {
                    myChart.resize();
                });
            }

            function threadChart() {
                const myChart = echarts.init(document.getElementById('threadChart'));
                myChart.setOption({
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {
                            lineStyle: {
                                color: '#57617B'
                            }
                        }
                    },
                    grid: {
                        left: '0',
                        right: '20',
                        top: '10',
                        bottom: '20',
                        containLabel: true
                    },
                    xAxis: [{
                        type: 'category',
                        boundaryGap: false,
                        axisLabel: {
                            show: true,
                            textStyle: {
                                color: 'rgba(255,255,255,.6)'
                            }
                        },
                        axisLine: {
                            lineStyle: {
                                color: 'rgba(255,255,255,.1)'
                            }
                        },
                        data: ${threadInfo.strXAxis}
                    }, {}],
                    yAxis: [{
                        axisLabel: {
                            show: true,
                            textStyle: {
                                color: 'rgba(255,255,255,.6)'
                            }
                        },
                        axisLine: {
                            lineStyle: {
                                color: 'rgba(255,255,255,.1)'
                            }
                        },
                        splitLine: {
                            lineStyle: {
                                color: 'rgba(255,255,255,.1)'
                            }
                        }
                    }],
                    series: [{
                        name: '线程数',
                        type: 'line',
                        smooth: true,
                        symbol: 'circle',
                        symbolSize: 5,
                        showSymbol: false,
                        lineStyle: {
                            normal: {
                                width: 2
                            }
                        },
                        areaStyle: {
                            normal: {
                                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                                    offset: 0,
                                    color: 'rgba(24, 163, 64, 0.3)'
                                }, {
                                    offset: 0.8,
                                    color: 'rgba(24, 163, 64, 0)'
                                }], false),
                                shadowColor: 'rgba(0, 0, 0, 0.1)',
                                shadowBlur: 10
                            }
                        },
                        itemStyle: {
                            normal: {
                                color: '#cdba00',
                                borderColor: 'rgba(137,189,2,0.27)',
                                borderWidth: 12
                            }
                        },
                        data: ${threadInfo.strThreadCount}
                    }]
                });
                window.addEventListener("resize", function () {
                    myChart.resize();
                });
            }

            function consumerChart() {
                const myChart = echarts.init(document.getElementById('consumerChart'));
                myChart.setOption({
                    tooltip: {
                        show: false
                    },
                    grid: {
                        top: '0%',
                        left: '200',
                        right: '14%',
                        bottom: '0%'
                    },
                    xAxis: {
                        min: 0,
                        max: 100,
                        splitLine: {show: false},
                        axisTick: {show: false},
                        axisLine: {show: false},
                        axisLabel: {show: false}
                    },
                    yAxis: {
                        data: ${barInfo.strYData},
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        },
                        axisLabel: {
                            color: 'rgba(255,255,255,.6)',
                            fontSize: 14
                        }
                    },
                    series: [{
                        type: 'bar',
                        label: {
                            show: true,
                            zlevel: 10000,
                            position: 'right',
                            padding: 10,
                            color: '#49bcf7',
                            fontSize: 14,
                        },
                        itemStyle: {
                            color: '#49bcf7'
                        },
                        barWidth: '15',
                        data: ${barInfo.strSeries},
                        z: 10
                    }],
                });
                window.addEventListener("resize", function () {
                    myChart.resize();
                });
            }

            function zkCount() {
                const myChart = echarts.init(document.getElementById('zkCount'));
                myChart.setOption({
                    series: [{
                        type: 'pie',
                        radius: ['60%', '70%'],
                        color: '#49bcf7',
                        label: {
                            normal: {
                                position: 'center'
                            }
                        },
                        data: [{
                            value: ${zkCount},
                            label: {
                                normal: {
                                    formatter: ${zkCount} +'',
                                    textStyle: {
                                        fontSize: 20,
                                        color: '#fff',
                                    }
                                }
                            }
                        }]
                    }]
                });
                window.addEventListener("resize", function () {
                    myChart.resize();
                });
            }

            function kafkaCount() {
                const myChart = echarts.init(document.getElementById('kafkaCount'));
                myChart.setOption({
                    series: [{
                        type: 'pie',
                        radius: ['60%', '70%'],
                        color: '#cdba00',
                        label: {
                            normal: {
                                position: 'center'
                            }
                        },
                        data: [{
                            value: ${kafkaCount},
                            label: {
                                normal: {
                                    formatter: ${kafkaCount} +'',
                                    textStyle: {
                                        fontSize: 20,
                                        color: '#fff',
                                    }
                                }
                            }
                        }]
                    }]
                });
                window.addEventListener("resize", function () {
                    myChart.resize();
                });
            }

            function topicCount() {
                const myChart = echarts.init(document.getElementById('topicCount'));
                myChart.setOption({
                    series: [{
                        type: 'pie',
                        radius: ['60%', '70%'],
                        color: '#62c98d',
                        label: {
                            normal: {
                                position: 'center'
                            }
                        },
                        data: [{
                            value: ${topicCount},
                            name: '女消费',
                            label: {
                                normal: {
                                    formatter: ${topicCount} +'',
                                    textStyle: {
                                        fontSize: 20,
                                        color: '#fff',
                                    }
                                }
                            }
                        }]
                    }]
                });
                window.addEventListener("resize", function () {
                    myChart.resize();
                });
            }
        });
    </script>
    </body>
    </html>
</@compress>