package com.pegasus.kafka.job;

import com.pegasus.kafka.service.dto.SysLagService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component

public class TopicSchedule {
    private final SysLogSizeService sysLogSizeService;
    private final SysLagService sysLagService;


    public TopicSchedule(SysLogSizeService sysLogSizeService, SysLagService sysLagService) {
        this.sysLogSizeService = sysLogSizeService;
        this.sysLagService = sysLagService;
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void lagCollect() throws Exception {
        SysLogSizeService.Matrix matrix = sysLogSizeService.collect();
        sysLogSizeService.saveBatch(matrix.getSysLogSizeList());
        sysLagService.saveBatch(matrix.getSysLagList());
    }

}