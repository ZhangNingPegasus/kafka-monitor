package com.pegasus.kafka.job;

import com.pegasus.kafka.service.dto.SchemaService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final Set<String> NO_NEED_TO_DELETE_EXPIRE_RECORDS_TABLES = new HashSet<>(Arrays.asList("sys_admin"));
    private final SysLogSizeService sysLogSizeService;
    private final SchemaService schemaService;

    public DeleteSchedule(SysLogSizeService sysLogSizeService, SchemaService schemaService) {
        this.sysLogSizeService = sysLogSizeService;
        this.schemaService = schemaService;
    }

    //每天00:01:00执行一次
    @Scheduled(cron = "0 1 0 1/1 * ?")
    public void deleteExpired() {
        Set<String> tableNames = schemaService.listTables();
        Set<String> filterTableNames = tableNames.stream().filter(p -> !NO_NEED_TO_DELETE_EXPIRE_RECORDS_TABLES.contains(p)).collect(Collectors.toSet());
        schemaService.deleteExpired(filterTableNames);
    }
}
