package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.entity.dto.SysLogSize;
import com.pegasus.kafka.entity.echarts.LineInfo;
import com.pegasus.kafka.service.dto.SysLagService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
import com.pegasus.kafka.service.kafka.KafkaTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("dashboard")
public class DashboardController {
    private final KafkaConsumerService kafkaConsumerService;
    private final KafkaTopicService kafkaTopicService;
    private final SysLagService sysLagService;
    private final SysLogSizeService sysLogSizeService;

    public DashboardController(KafkaConsumerService kafkaConsumerService, KafkaTopicService kafkaTopicService, SysLagService sysLagService, SysLogSizeService sysLogSizeService) {
        this.kafkaConsumerService = kafkaConsumerService;
        this.kafkaTopicService = kafkaTopicService;
        this.sysLagService = sysLagService;
        this.sysLogSizeService = sysLogSizeService;
    }

    @RequestMapping("index")
    public String index(Model model) throws Exception {
        model.addAttribute("consumers", kafkaConsumerService.listKafkaConsumers());
        model.addAttribute("topics", kafkaTopicService.listTopicNames());
        return "dashboard/index";
    }

    @RequestMapping("getLagChart")
    @ResponseBody
    public Result<LineInfo> getLagChart(@RequestParam(name = "groupId", required = true) String groupId,
                                        @RequestParam(name = "createTimeRange", required = true) String createTimeRange) throws ParseException {
        groupId = groupId.trim();
        createTimeRange = createTimeRange.trim();
        if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(createTimeRange)) {
            return Result.success();
        }
        LineInfo result = new LineInfo();
        Common.TimeRange timeRange = Common.splitTime(createTimeRange);
        Date from = timeRange.getStart(), to = timeRange.getEnd();

        List<SysLag> sysLagList = sysLagService.listByGroupId(groupId, from, to);
        List<String> topicNames = sysLagList.stream().map(SysLag::getTopicName).distinct().collect(Collectors.toList());
        List<String> times = sysLagList.stream().map(p -> Common.format(p.getCreateTime())).distinct().collect(Collectors.toList());

        result.setTopicNames(topicNames);
        result.setTimes(times);

        List<LineInfo.Series> seriesList = new ArrayList<>(result.getTopicNames().size());
        for (String topicName : result.getTopicNames()) {
            LineInfo.Series series = new LineInfo.Series();
            series.setName(topicName);
            series.setType("line");
            series.setSmooth(true);
            seriesList.add(series);
        }
        result.setSeries(seriesList);

        for (LineInfo.Series series : result.getSeries()) {
            List<Double> data = new ArrayList<>(result.getTimes().size());
            for (String time : result.getTimes()) {
                List<Double> lag = sysLagList.stream().filter(p -> p.getTopicName().equals(series.getName()) && Common.format(p.getCreateTime()).equals(time)).map(p -> Double.parseDouble(p.getLag().toString())).collect(Collectors.toList());
                if (lag.size() < 1) {
                    data.add(null);
                } else {
                    data.addAll(lag);
                }
            }
            series.setData(data);
        }
        return Result.success(result);
    }

    @RequestMapping("getTopicChart")
    @ResponseBody
    public Result<LineInfo> getTopicChart(@RequestParam(name = "topicName", required = true) String topicName,
                                          @RequestParam(name = "createTimeRange", required = true) String createTimeRange) throws ParseException {
        topicName = topicName.trim();
        createTimeRange = createTimeRange.trim();
        if (StringUtils.isEmpty(createTimeRange)) {
            return Result.success();
        }
        LineInfo result = new LineInfo();
        Common.TimeRange timeRange = Common.splitTime(createTimeRange);
        Date from = timeRange.getStart(), to = timeRange.getEnd();
        List<SysLogSize> sysLogSizeList = sysLogSizeService.listByTopicName(topicName.equals("所有主题") ? null : topicName, from, to);

        List<String> topicNames = sysLogSizeList.stream().map(SysLogSize::getTopicName).distinct().collect(Collectors.toList());
        List<String> times = sysLogSizeList.stream().map(p -> Common.format(p.getCreateTime())).distinct().collect(Collectors.toList());

        result.setTopicNames(topicNames);
        result.setTimes(times);

        List<LineInfo.Series> seriesList = new ArrayList<>(result.getTopicNames().size());
        for (String name : result.getTopicNames()) {
            LineInfo.Series series = new LineInfo.Series();
            series.setName(name);
            series.setType("line");
            series.setSmooth(true);
            seriesList.add(series);
        }
        result.setSeries(seriesList);

        for (LineInfo.Series series : result.getSeries()) {
            List<Double> data = new ArrayList<>(result.getTimes().size());
            for (String time : result.getTimes()) {
                List<Double> logSize = sysLogSizeList.stream().filter(p -> p.getTopicName().equals(series.getName()) && Common.format(p.getCreateTime()).equals(time)).map(p -> Double.parseDouble(p.getLogSize().toString())).collect(Collectors.toList());
                if (logSize.size() < 1) {
                    data.add(null);
                } else {
                    data.addAll(logSize);
                }
            }
            series.setData(data);
        }

        return Result.success(result);
    }

    @RequestMapping("getTopicRankChart")
    @ResponseBody
    public Result<LineInfo> getTopicRankChart() {
        LineInfo result = new LineInfo();

        List<SysLogSize> sysLogSizeList = sysLogSizeService.getTopicRank(5);

        List<String> topicNames = sysLogSizeList.stream().map(SysLogSize::getTopicName).collect(Collectors.toList());
        List<Double> logSizeList = sysLogSizeList.stream().map(p -> Double.parseDouble(p.getLogSize().toString())).collect(Collectors.toList());

        List<LineInfo.Series> seriesList = new ArrayList<>();

        LineInfo.Series series = new LineInfo.Series();
        series.setType("bar");
        series.setData(logSizeList);
        seriesList.add(series);

        result.setTopicNames(topicNames);
        result.setSeries(seriesList);
        return Result.success(result);
    }
}