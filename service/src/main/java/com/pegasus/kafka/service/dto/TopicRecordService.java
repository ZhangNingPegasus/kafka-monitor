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

    public void batchSave(List<TopicRecord> topicRecordList) {
        if (topicRecordList.size() < 1) {
            return;
        }
        batchSave(analyse(topicRecordList));
    }

    @TranSave
    public void batchSave(Map<String, List<TopicRecord>> topicRecordMap) {
        if (topicRecordMap.size() < 1) {
            return;
        }

        Set<String> topicNames = topicRecordMap.keySet();
        createTableIfNotExists(topicNames);

        for (Map.Entry<String, List<TopicRecord>> entry : topicRecordMap.entrySet()) {
            String topicName = entry.getKey();
            List<TopicRecord> topicRecordList = entry.getValue();
            this.baseMapper.batchSave(convertToTableName(topicName), topicRecordList);
        }
    }

    @TranSave
    public void createTableIfNotExists(Set<String> topicNames) {
        this.baseMapper.createTableIfNotExists(convertToTableName(topicNames));
    }

    @TranRead
    public void dropTable(String topicName) {
        this.baseMapper.dropTable(convertToTableName(topicName));
    }

    private Map<String, List<TopicRecord>> analyse(List<TopicRecord> topicRecordList) {
        Map<String, List<TopicRecord>> result = new LinkedHashMap<>(topicRecordList.size());
        for (TopicRecord topicRecord : topicRecordList) {
            String topicName = topicRecord.getTopicName();
            if (result.containsKey(topicName)) {
                result.get(topicName).add(topicRecord);
            } else {
                List<TopicRecord> topicRecords = new ArrayList<>();
                topicRecords.add(topicRecord);
                result.put(topicName, topicRecords);
            }
        }
        return result;
    }

    @TranRead
    public List<TopicRecord> listMessages(IPage page, String topicName, Integer partitionId, String key, Date from, Date to) {
        return this.baseMapper.listMessages(page, convertToTableName(topicName), partitionId, key, from, to);
    }

    @TranRead
    public TopicRecord findMessage(String topicName, Integer partitionId, Long offset, String key) {
        return this.baseMapper.findMessage(convertToTableName(topicName), partitionId, offset, key);
    }

    private String convertToTableName(String topicName) {
        return String.format("topic_%s", topicName);
    }

    private Set<String> convertToTableName(Set<String> topicNames) {
        Set<String> result = new HashSet<>(topicNames.size());
        for (String topicName : topicNames) {
            result.add(convertToTableName(topicName));
        }
        return result;
    }
}
