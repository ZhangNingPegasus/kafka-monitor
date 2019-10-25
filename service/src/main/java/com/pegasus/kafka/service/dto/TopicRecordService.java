package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.mapper.TopicRecordMapper;
import org.springframework.stereotype.Service;

import java.util.*;

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
        checkTableExists(topicNames);

        for (Map.Entry<String, List<TopicRecord>> entry : topicRecordMap.entrySet()) {
            String topicName = entry.getKey();
            List<TopicRecord> topicRecordList = entry.getValue();
            this.baseMapper.batchSave(topicName, topicRecordList);
        }
    }

    @TranSave
    public void checkTableExists(Set<String> tableNames) {
        this.baseMapper.createTableIfNotExists(tableNames);
    }

    @TranSave
    public void createDatabaseIfNotExists() {
        this.baseMapper.createDatabaseIfNotExists();
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
    public List<TopicRecord> listMessages(IPage page, String tableName, Integer partitionId, String key, Date from, Date to) {
        return this.baseMapper.listMessages(page, tableName, partitionId, key, from, to);
    }

    @TranRead
    public TopicRecord findMessage(String topicName, Integer partitionId, Long offset, String key) {
        return this.baseMapper.findMessage(topicName, partitionId, offset, key);
    }
}
