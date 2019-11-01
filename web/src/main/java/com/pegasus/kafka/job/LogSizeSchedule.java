package com.pegasus.kafka.job;

import com.pegasus.kafka.service.dto.SysLagService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component

public class LogSizeSchedule {
    private final SysLogSizeService sysLogSizeService;
    private final SysLagService sysLagService;


    public LogSizeSchedule(SysLogSizeService sysLogSizeService, SysLagService sysLagService) {
        this.sysLogSizeService = sysLogSizeService;
        this.sysLagService = sysLagService;
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void collect() throws Exception {
        SysLogSizeService.Matrix matrix = sysLogSizeService.kpi(new Date());
        try {
            sysLogSizeService.saveBatch(matrix.getSysLogSizeList());
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        try {
            sysLagService.saveBatch(matrix.getSysLagList());
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

}