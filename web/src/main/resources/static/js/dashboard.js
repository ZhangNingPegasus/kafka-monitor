function initTopicSendChart(data) {
    return {
        title: {
            text: '发送速率'
        },
        grid: {
            left: '2%',
            right: '2%',
            bottom: 40,
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
            name: "每秒消息发送数",
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
    };
}

function initLagChart(data) {
    return {
        title: {
            text: '堆积速率'
        },
        grid: {
            left: '2%',
            right: '2%',
            bottom: 40,
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
            name: '每秒消息堆积数',
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
    };
}

function initConsumeTpsChart(data) {
    return {
        title: {
            text: '消费速率'
        },
        grid: {
            left: '2%',
            right: '2%',
            bottom: 40,
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
            name: '每秒消息消费数',
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
    };
}

function initTopicRankChart(data) {
    return {
        title: {
            text: '主题前5排行榜'
        },
        grid: {
            left: '2%',
            right: '2%',
            bottom: 40,
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
                return str;
            })
        },
        yAxis: {
            name: '发送总量最多的前5个Topic',
            type: 'value'
        },
        series: data.series
    };
}

function initTopicHistoryChart(data) {
    return {
        title: {
            text: '最近7天消息发送数量'
        },
        grid: {
            left: '2%',
            right: '2%',
            bottom: 40,
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
            data: data.times.map(function (str) {
                if (str.length > 15) {
                    return str.substr(0, 15) + '...'
                } else {
                    return str;
                }
            })
        },
        yAxis: {
            name: '每天消息发送总量',
            type: 'value'
        },
        series: data.series
    };
}