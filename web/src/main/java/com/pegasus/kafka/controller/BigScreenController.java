package com.pegasus.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysKpi;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.entity.echarts.BarInfo;
import com.pegasus.kafka.entity.echarts.CpuInfo;
import com.pegasus.kafka.entity.echarts.ThreadInfo;
import com.pegasus.kafka.entity.vo.TopicRecordCountVo;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.dto.SysKpiService;
import com.pegasus.kafka.service.dto.SysLagService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import com.pegasus.kafka.service.property.PropertyService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.pegasus.kafka.controller.BigScreenController.PREFIX;


/**
 * The controller for big screen.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class BigScreenController {
    public static final String PREFIX = "bigscreen";
    private final SysLogSizeService sysLogSizeService;
    private final PropertyService propertyService;
    private final SysKpiService sysKpiService;
    private final KafkaService kafkaService;
    private final SysLagService sysLagService;

    public BigScreenController(SysLogSizeService sysLogSizeService, PropertyService propertyService, SysKpiService sysKpiService, KafkaService kafkaService, SysLagService sysLagService) {
        this.sysLogSizeService = sysLogSizeService;
        this.propertyService = propertyService;
        this.sysKpiService = sysKpiService;
        this.kafkaService = kafkaService;
        this.sysLagService = sysLagService;
    }

    @GetMapping("tolist")
    public String toList(Model model) throws Exception {
        Long day0 = sysLogSizeService.getTotalRecordCount();
        int zkCount = propertyService.getZookeeper().split(",").length;
        int kafkaCount = kafkaService.listBrokerNames().size();
        int topicCount = kafkaService.listTopicNames().size();
        List<TopicRecordCountVo> topicRecordCountVoList = sysLogSizeService.listTotalRecordCount(25);
        for (TopicRecordCountVo topicRecordCountVo : topicRecordCountVoList) {
            String topicName = topicRecordCountVo.getTopicName();
            int i = topicName.lastIndexOf(".");
            if (i >= 0) {
                if (i + 1 <= topicName.length() - 1) {
                    topicRecordCountVo.setTopicName(topicName.substring(i + 1));
                }
            }
        }

        List<SysLag> sysLagList = sysLagService.listTopLag(25);
        BarInfo barInfo = new BarInfo();
        List<String> yDataList = new ArrayList<>(sysLagList.size());
        List<Long> seriesList = new ArrayList<>(sysLagList.size());
        for (SysLag sysLag : sysLagList) {
            yDataList.add(sysLag.getConsumerName());
            seriesList.add(sysLag.getLag());
        }
        barInfo.setYData(yDataList);
        barInfo.setSeries(seriesList);
        barInfo.setStrYData(JSON.toJSON(yDataList).toString());
        barInfo.setStrSeries(JSON.toJSON(seriesList).toString());


        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                0,
                0,
                0);
        Date from = calendar.getTime();
        Date to = DateUtils.addDays(from, 1);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        List<SysKpi> sysKpiList = sysKpiService.listKpi(Arrays.asList(SysKpi.KAFKA_KPI.KAFKA_SYSTEM_CPU_LOAD.getCode(), SysKpi.KAFKA_KPI.KAFKA_PROCESS_CPU_LOAD.getCode()),
                DateUtils.addMinutes(new Date(), -60),
                to
        );
        CpuInfo cpuInfo = new CpuInfo();
        cpuInfo.setXAxis(sysKpiList.stream().map(p -> sdf.format(p.getCreateTime())).distinct().collect(Collectors.toList()));
        cpuInfo.setSystemCpu(sysKpiList.stream().filter(p -> p.getKpi() == SysKpi.KAFKA_KPI.KAFKA_SYSTEM_CPU_LOAD.getCode()).map(p -> Common.numberic(p.getValue())).collect(Collectors.toList()));
        cpuInfo.setProcessCpu(sysKpiList.stream().filter(p -> p.getKpi() == SysKpi.KAFKA_KPI.KAFKA_PROCESS_CPU_LOAD.getCode()).map(p -> Common.numberic(p.getValue())).collect(Collectors.toList()));

        cpuInfo.setStrXAxis(JSON.toJSONString(cpuInfo.getXAxis()));
        cpuInfo.setStrSystemCpu(JSON.toJSONString(cpuInfo.getSystemCpu()));
        cpuInfo.setStrProcessCpu(JSON.toJSONString(cpuInfo.getProcessCpu()));

        List<SysKpi> threadInfoList = sysKpiService.listKpi(Collections.singletonList(SysKpi.KAFKA_KPI.KAFKA_THREAD_COUNT.getCode()), DateUtils.addMinutes(new Date(), -60), to);
        ThreadInfo threadInfo = new ThreadInfo();
        threadInfo.setXAxis(threadInfoList.stream().map(p -> sdf.format(p.getCreateTime())).distinct().collect(Collectors.toList()));
        threadInfo.setThreadCount(threadInfoList.stream().map(p -> p.getValue().intValue()).collect(Collectors.toList()));

        threadInfo.setStrXAxis(JSON.toJSONString(threadInfo.getXAxis()));
        threadInfo.setStrThreadCount(JSON.toJSONString(threadInfo.getThreadCount()));

        model.addAttribute("savingDays", propertyService.getDbRetentionDays());
        model.addAttribute("totalRecordCount", day0);
        model.addAttribute("zkCount", zkCount);
        model.addAttribute("kafkaCount", kafkaCount);
        model.addAttribute("topicCount", topicCount);
        model.addAttribute("barInfo", barInfo);
        model.addAttribute("topicRecordCountVoList", topicRecordCountVoList);
        model.addAttribute("cpuInfo", cpuInfo);
        model.addAttribute("threadInfo", threadInfo);
        return String.format("%s/%s", PREFIX, "list");
    }

}
