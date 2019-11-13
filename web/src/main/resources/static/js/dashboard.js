function topicChart(data) {
    return {
        title: {
            text: '消息TPS图'
        },
        grid: {
            left: '4%',
            right: '9%',
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

function lagChart(data) {
    return {
        title: {
            text: '消息堆积图'
        },
        grid: {
            left: '4%',
            right: '9%',
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

function topicRankChart(data) {
    return {
        title: {
            text: '主题前5排行榜'
        },
        grid: {
            left: '4%',
            right: '9%',
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
            type: 'value'
        },
        series: data.series
    };
}

function topicHistoryChart(data) {
    return {
        title: {
            text: '最近7天消息记录'
        },
        grid: {
            left: '4%',
            right: '9%',
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
            type: 'value'
        },
        series: data.series
    };
}