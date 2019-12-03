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
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.pegasus.kafka.controller.ZkPerformanceController.PREFIX;

/**
 * The controller for showing the performance of zookeeper.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class ZkPerformanceController {
    public static final String PREFIX = "zkperformance";
    private static final List<Integer> ZK_KPI;

    static {
        ZK_KPI = new ArrayList<>(SysKpi.ZK_KPI.values().length);
        for (SysKpi.ZK_KPI value : SysKpi.ZK_KPI.values()) {
            ZK_KPI.add(value.getCode());
        }
    }

    private final SysKpiService sysKpiService;
    private final EhcacheService ehcacheService;

    public ZkPerformanceController(SysKpiService sysKpiService, EhcacheService ehcacheService) {
        this.sysKpiService = sysKpiService;
        this.ehcacheService = ehcacheService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @PostMapping("getChart")
    @ResponseBody
    public Result<ZooKeeperPerformance> getChart(@RequestParam(name = "createTimeRange", required = true) String createTimeRange) throws ParseException {
        String key = String.format("ZkPerformanceController::getChart::%s", createTimeRange);
        ZooKeeperPerformance result = ehcacheService.get(key);
        if (result == null) {
            result = new ZooKeeperPerformance();
            Common.TimeRange timeRange = Common.splitTime(createTimeRange);
            Date from = timeRange.getStart(), to = timeRange.getEnd();
            List<SysKpi> sysKpiList = sysKpiService.listKpi(ZK_KPI, from, to);

            result.setSend(getInfo(sysKpiList, SysKpi.ZK_KPI.ZK_PACKETS_SENT));
            result.setReceived(getInfo(sysKpiList, SysKpi.ZK_KPI.ZK_PACKETS_RECEIVED));
            result.setAlive(getInfo(sysKpiList, SysKpi.ZK_KPI.ZK_NUM_ALIVE_CONNECTIONS));
            result.setQueue(getInfo(sysKpiList, SysKpi.ZK_KPI.ZK_OUTSTANDING_REQUESTS));
            ehcacheService.set(key, result);
        }
        return Result.ok(result);
    }

    private LineInfo getInfo(List<SysKpi> sysKpiList, SysKpi.ZK_KPI kpi) {
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
    private static class ZooKeeperPerformance {
        private LineInfo send;
        private LineInfo received;
        private LineInfo alive;
        private LineInfo queue;
    }
}