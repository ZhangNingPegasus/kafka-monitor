package com.pegasus.kafka.job;

import com.pegasus.kafka.service.dto.SchemaService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * The schedule job for deleting expired records.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Component
public class DeleteSchedule {

    private final SysLogSizeService sysLogSizeService;
    private final SchemaService schemaService;

    public DeleteSchedule(SysLogSizeService sysLogSizeService, SchemaService schemaService) {
        this.sysLogSizeService = sysLogSizeService;
        this.schemaService = schemaService;
    }

    @Scheduled(cron = "0 1 0 1/1 * ?")
    public void deleteExpired() {
        Set<String> tableNames = schemaService.listTables();
        schemaService.deleteExpired(tableNames);
    }
}
