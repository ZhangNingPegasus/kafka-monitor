package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.ehcache.EhcacheService;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysKpi;
import com.pegasus.kafka.entity.echarts.LineInfo;
import com.pegasus.kafka.service.dto.SysKpiService;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.pegasus.kafka.controller.KafkaPerformanceController.PREFIX;

/**
 * The controller for showing the performance of kafka's information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class KafkaPerformanceController {
    public static final String PREFIX = "kafkaperformance";
    private static final List<Integer> KAFKA_KPI;

    static {
        KAFKA_KPI = new ArrayList<>(SysKpi.KAFKA_KPI.values().length);
        for (SysKpi.KAFKA_KPI value : SysKpi.KAFKA_KPI.values()) {
            KAFKA_KPI.add(value.getCode());
        }
    }

    private final EhcacheService ehcacheService;
    private final SysKpiService sysKpiService;

    public KafkaPerformanceController(EhcacheService ehcacheService, SysKpiService sysKpiService) {
        this.ehcacheService = ehcacheService;
        this.sysKpiService = sysKpiService;
    }

    @RequestMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @PostMapping("getChart")
    @ResponseBody
    public Result<KafkaPerformance> getZkSendChart(@RequestParam(name = "createTimeRange", required = true) String createTimeRange) throws ParseException {
        String key = String.format("KafkaPerformanceController::getZkSendChart::%s", createTimeRange);
        KafkaPerformance result = ehcacheService.get(key);
        if (result == null) {
            result = new KafkaPerformance();
            Common.TimeRange timeRange = Common.splitTime(createTimeRange);
            Date from = timeRange.getStart(), to = timeRange.getEnd();
            List<SysKpi> sysKpiList = sysKpiService.listKpi(KAFKA_KPI, from, to);

            result.setMsgIn(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_MESSAGES_IN));
            result.setBytesIn(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_BYTES_IN));
            result.setBytesOut(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_BYTES_OUT));
            result.setBytesRejected(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_BYTES_REJECTED));
            result.setFailedFetchRequest(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_FAILED_FETCH_REQUEST));
            result.setFailedProduceRequest(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_FAILED_PRODUCE_REQUEST));
            result.setProduceMessageConversions(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_PRODUCE_MESSAGE_CONVERSIONS));
            result.setTotalFetchRequests(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_TOTAL_FETCH_REQUESTS_PER_SEC));
            result.setTotalProduceRequests(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_TOTAL_PRODUCE_REQUESTS_PER_SEC));
            result.setReplicationBytesOut(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_REPLICATION_BYTES_OUT_PER_SEC));
            result.setReplicationBytesIn(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_REPLICATION_BYTES_IN_PER_SEC));
            result.setOsFreeMemory(getInfo(sysKpiList, SysKpi.KAFKA_KPI.KAFKA_OS_USED_MEMORY_PERCENTAGE));
            ehcacheService.set(key, result);
        }
        return Result.success(result);
    }

    private LineInfo getInfo(List<SysKpi> sysKpiList, SysKpi.KAFKA_KPI kpi) {
        LineInfo result = new LineInfo();

        List<String> times = sysKpiList.stream().map(p -> Common.format(p.getCreateTime())).distinct().collect(Collectors.toList());
        result.setTimes(times);

        List<LineInfo.Series> seriesList = new ArrayList<>(1);

        LineInfo.Series series = new LineInfo.Series();
        series.setName("");
        series.setType("line");
        series.setSmooth(true);
        series.setAreaStyle(new JSONObject());

        List<Double> data = new ArrayList<>(result.getTimes().size());
        List<Double> val = sysKpiList.stream().filter(p -> p.getKpi().equals(kpi.getCode())).map(SysKpi::getValue).collect(Collectors.toList());
        data.addAll(val);
        series.setData(data);
        seriesList.add(series);
        result.setSeries(seriesList);

        return result;
    }

    @Data
    private static class KafkaPerformance {
        private LineInfo msgIn;
        private LineInfo bytesIn;
        private LineInfo bytesOut;
        private LineInfo bytesRejected;
        private LineInfo failedFetchRequest;
        private LineInfo failedProduceRequest;
        private LineInfo produceMessageConversions;
        private LineInfo totalFetchRequests;
        private LineInfo totalProduceRequests;
        private LineInfo replicationBytesOut;
        private LineInfo replicationBytesIn;
        private LineInfo osFreeMemory;
    }
}