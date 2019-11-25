package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.mapper.TopicRecordMapper;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * The service for dynamic table. Saving topics' records.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class TopicRecordService extends ServiceImpl<TopicRecordMapper, TopicRecord> {
    private final SchemaService schemaService;

    public TopicRecordService(SchemaService schemaService) {
        this.schemaService = schemaService;
    }


    @TranSave
    public void batchSave(String topicName, List<TopicRecord> topicRecordList) {
        String tableName = convertToTableName(topicName);
        Set<String> tableNames = schemaService.listTables();
        if (!tableNames.contains(tableName)) {
            createTableIfNotExists(new HashSet<>(Arrays.asList(tableName)));
        }
        this.baseMapper.batchSave(tableName, topicRecordList);
    }

    @TranSave
    public void createTableIfNotExists(Set<String> tableNames) {
        this.baseMapper.createTableIfNotExists(tableNames);
    }

    @TranRead
    public void dropTable(String topicName) {
        this.baseMapper.dropTable(convertToTableName(topicName));
    }

    @TranRead
    public List<TopicRecord> listMessages(IPage page, String topicName, Integer partitionId, String key, Date from, Date to) {
        try {
            return this.baseMapper.listMessages(page, convertToTableName(topicName), partitionId, key, from, to);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @TranRead
    public TopicRecord findMessage(String topicName, Integer partitionId, Long offset, String key) {
        try {
            return this.baseMapper.findMessage(convertToTableName(topicName), partitionId, offset, key);
        } catch (Exception e) {
            return null;
        }
    }

    public String convertToTableName(String topicName) {
        return String.format("topic_%s", topicName);
    }

    public Set<String> convertToTableName(Set<String> topicNames) {
        Set<String> result = new HashSet<>(topicNames.size());
        for (String topicName : topicNames) {
            result.add(convertToTableName(topicName));
        }
        return result;
    }
}
