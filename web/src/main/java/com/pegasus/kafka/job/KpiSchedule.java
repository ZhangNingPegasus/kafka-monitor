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

    @Scheduled(cron = "0 0/1 * * * ?")
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