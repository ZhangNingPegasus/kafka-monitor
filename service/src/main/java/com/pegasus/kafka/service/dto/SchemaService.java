package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.mapper.SchemaMapper;
import com.pegasus.kafka.service.property.PropertyService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

/**
 * The service for initiate the database and tables.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SchemaService extends ServiceImpl<SchemaMapper, TopicRecord> {
    private final PropertyService propertyService;

    public SchemaService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @TranSave
    public void createTableIfNotExists() {
        this.baseMapper.createTableIfNotExists(propertyService.getDbName());
    }

    @TranSave
    public void deleteExpired(Set<String> tableNames) {
        Date now = new Date();
        Date date = DateUtils.addDays(now, -propertyService.getDbRetentionDays());
        this.baseMapper.deleteExpired(propertyService.getDbName(), tableNames, date);
    }

    @TranRead
    public Set<String> listTables() {
        String databaseName = Common.trim(propertyService.getDbName(), '`');
        return this.baseMapper.listTables(databaseName);
    }
}
