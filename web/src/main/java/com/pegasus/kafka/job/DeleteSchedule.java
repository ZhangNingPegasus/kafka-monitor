package com.pegasus.kafka.job;

import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.dto.SchemaService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import com.pegasus.kafka.service.dto.TopicRecordService;
import com.pegasus.kafka.service.property.PropertyService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
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

    private final Set<String> NO_NEED_TO_DELETE_EXPIRE_RECORDS_TABLES = new HashSet<>(Arrays.asList("sys_admin", "sys_alert_cluster", "sys_alert_consumer", "sys_dingding_config", "sys_mail_config", "sys_page", "sys_permission", "sys_role"));
    private final SchemaService schemaService;
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;
    private final PropertyService propertyService;

    public DeleteSchedule(SysLogSizeService sysLogSizeService, SchemaService schemaService, KafkaService kafkaService, TopicRecordService topicRecordService, PropertyService propertyService) {
        this.schemaService = schemaService;
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
        this.propertyService = propertyService;
    }

    //每天00:01:00执行一次
    @Scheduled(cron = "0 1 0 1/1 * ?")
    public void deleteExpired() {
        Set<String> tableNames = schemaService.listTables();
        Set<String> filterTableNames = tableNames.stream().filter(p -> !NO_NEED_TO_DELETE_EXPIRE_RECORDS_TABLES.contains(p)).collect(Collectors.toSet());
        schemaService.deleteExpired(filterTableNames);
    }

    //每天凌晨3点执行
    @Scheduled(cron = "0 0 3 * * ?")
    public void dropUnusedTable() throws Exception {
        Set<String> dbtableNames = schemaService.listTables();
        Set<String> filterDbTableNames = dbtableNames.stream().filter(p -> p.contains(TopicRecordService.TABLE_PREFIX) && !NO_NEED_TO_DELETE_EXPIRE_RECORDS_TABLES.contains(p)).collect(Collectors.toSet());
        List<String> topicTableNames = kafkaService.listTopicNames().stream().map(topicRecordService::convertToTableName).collect(Collectors.toList());

        List<String> needDropList = new ArrayList<>();

        for (String dbTableName : filterDbTableNames) {
            if (!topicTableNames.contains(dbTableName)) {
                Date maxCreateTime = topicRecordService.getMaxCreateTime(dbTableName);
                if (maxCreateTime == null || maxCreateTime.before(DateUtils.addDays(new Date(), -propertyService.getDbRetentionDays()))) {
                    needDropList.add(dbTableName);
                }
            }
        }

        for (String tableName : needDropList) {
            topicRecordService.getBaseMapper().truncateTable(tableName);
        }
    }
}