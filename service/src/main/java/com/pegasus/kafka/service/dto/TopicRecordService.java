package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.entity.dto.TopicRecordValue;
import com.pegasus.kafka.entity.po.MaxOffset;
import com.pegasus.kafka.mapper.TopicRecordMapper;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (topicRecordList == null || topicRecordList.size() < 1) {
            return;
        }
        String tableName = convertToTableName(topicName);
        String recordTableName = convertToRecordTableName(topicName);
        Set<String> tableNames = schemaService.listTables();
        if (!tableNames.contains(tableName)) {
            createTableIfNotExists(tableName);
        }
        if (!tableNames.contains(recordTableName)) {
            createRecordTableIfNotExists(recordTableName);
        }

        List<TopicRecordValue> topicRecordValueList = new ArrayList<>(topicRecordList.size());

        for (TopicRecord topicRecord : topicRecordList) {
            String value = topicRecord.getValue();

            TopicRecordValue topicRecordValue = new TopicRecordValue();
            topicRecordValue.setPartitionId(topicRecord.getPartitionId());
            topicRecordValue.setOffset(topicRecord.getOffset());
            topicRecordValue.setValue(value);
            topicRecordValueList.add(topicRecordValue);

            if (value.length() > 125) {
                value = value.substring(0, 125).concat("...");
                topicRecord.setValue(value);
            }
        }

        this.baseMapper.batchSave(tableName, topicRecordList);
        this.baseMapper.batchSaveRecord(recordTableName, topicRecordValueList);

    }


    @TranRead
    @Transactional
    public void dropTable(String topicName) {
        this.baseMapper.dropTable(convertToTableName(topicName));
        this.baseMapper.dropTable(convertToRecordTableName(topicName));
    }

    @TranRead
    public List<TopicRecord> listRecords(IPage page, String topicName, Integer partitionId, String key, Date from, Date to) {
        try {
            return this.baseMapper.listRecords(page, convertToTableName(topicName), partitionId, key, from, to);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @TranRead
    public TopicRecord findRecord(String topicName, Integer partitionId, Long offset, String key) {
        try {
            return this.baseMapper.findRecord(convertToTableName(topicName), partitionId, offset, key);
        } catch (Exception e) {
            return null;
        }
    }

    @TranRead
    public String findRecordValue(String topicName, Integer partitionId, Long offset) {
        try {
            return this.baseMapper.findRecordValue(convertToRecordTableName(topicName), partitionId, offset);
        } catch (Exception e) {
            return null;
        }
    }

    private void createTableIfNotExists(String tableName) {
        this.baseMapper.createTableIfNotExists(tableName);
    }

    private void createRecordTableIfNotExists(String recordTableName) {
        this.baseMapper.createRecordTableIfNotExists(recordTableName);
    }

    private String convertToTableName(String topicName) {
        return String.format("topic_%s", topicName);
    }

    private String convertToRecordTableName(String topicName) {
        return String.format("topic_record_%s", topicName);
    }


    public Long getRecordsCount(String topicName, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                0,
                0,
                0);
        Date now = calendar.getTime();

        Date from = DateUtils.addDays(now, -days);
        Date to = DateUtils.addDays(from, 1);
        Long result = this.baseMapper.getRecordsCount(convertToTableName(topicName), from, to);
        return result == null ? 0L : result;
    }

    @TranRead
    public List<MaxOffset> getMaxOffset(String topicName) {
        try {
            return this.baseMapper.getMaxOffset(convertToTableName(topicName));
        } catch (Exception ignored) {
            return null;
        }
    }
}
