package com.pegasus.kafka.job;

import com.pegasus.kafka.entity.dto.SysKpi;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.core.KafkaZkService;
import com.pegasus.kafka.service.dto.SysKpiService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The schedule job for collection kpi of kafka and zookeeper.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Component
public class KpiSchedule {
    private final KafkaZkService kafkaZkService;
    private final KafkaService kafkaService;
    private final SysKpiService sysKpiService;

    public KpiSchedule(KafkaZkService kafkaZkService, KafkaService kafkaService, SysKpiService sysKpiService) {
        this.kafkaZkService = kafkaZkService;
        this.kafkaService = kafkaService;
        this.sysKpiService = sysKpiService;
    }

    //每10分钟执行一次
    @Scheduled(cron = "0 0/5 * * * ?")
    public void collect() {
        Date now = new Date();
        List<SysKpi> sysKpiList = new ArrayList<>(32);
        try {
            sysKpiList.addAll(kafkaZkService.kpi(now));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        try {
            sysKpiList.addAll(kafkaService.kpi(now));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        sysKpiService.saveBatch(sysKpiList);
    }

}