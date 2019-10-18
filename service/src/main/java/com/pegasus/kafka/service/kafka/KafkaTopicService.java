package com.pegasus.kafka.service.kafka;

import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.entity.vo.KafkaConsumerInfo;
import com.pegasus.kafka.entity.vo.TopicVo;
import com.pegasus.kafka.service.core.KafkaService;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class KafkaTopicService {
    private final KafkaService kafkaService;

    public KafkaTopicService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public List<TopicVo> listTopicNames(String searchTopicName, SearchType searchType) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<TopicVo> topicInfoList = new ArrayList<>();
        List<String> topicNameList = kafkaService.listTopicNames();
        for (String topicName : topicNameList) {
            if (!StringUtils.isEmpty(searchTopicName)) {
                boolean isContinue = false;
                switch (searchType) {
                    case EQUALS:
                        isContinue = !topicName.equals(searchTopicName);
                        break;
                    case LIKE:
                        isContinue = !topicName.contains(searchTopicName);
                        break;
                }
                if (isContinue) {
                    continue;
                }
            }
            try {
                Stat stat = kafkaService.getTopicStat(topicName);
                List<String> partitionList = kafkaService.listPartitionIds(topicName);
                TopicVo topicInfo = new TopicVo();
                topicInfo.setName(topicName);
                topicInfo.setPartitionNum(partitionList.size());
                topicInfo.setPartitionIndex(partitionList.toString());
                topicInfo.setLogSize(kafkaService.getLogSize(topicName));
                topicInfo.setCreateTime(sdf.format(new Date(stat.getCtime())));
                topicInfo.setModifyTime(sdf.format(new Date(stat.getMtime())));
                topicInfoList.add(topicInfo);
            } catch (Exception e) {
                continue;
            }
        }
        return topicInfoList;
    }

    public List<TopicVo> listTopicNames() throws Exception {
        return listTopicNames(null, SearchType.EQUALS);
    }

    public void add(String topicName, Integer partitionNumber, Integer replicationNumber) throws Exception {
        try {
            kafkaService.createTopics(topicName, partitionNumber, replicationNumber);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOPIC_ALREADY_EXISTS);
        }
    }

    public void edit(String topicName, Integer partitionNumber) throws Exception {
        List<TopicVo> topicInfoList = listTopicNames(topicName, SearchType.EQUALS);
        if (topicInfoList != null && topicInfoList.size() > 0) {
            TopicVo topicVo = topicInfoList.get(0);
            if (partitionNumber > topicVo.getPartitionNum()) {
                kafkaService.alterTopics(topicName, partitionNumber);
            } else {
                throw new BusinessException(String.format("新的分区数量必须大于%s", topicVo.getPartitionNum()));
            }
        } else {
            throw new BusinessException(ResultCode.TOPIC_NOT_EXISTS);
        }
    }

    public void delete(String topicName) throws Exception {
        List<KafkaConsumerInfo> kafkaConsumerInfos = kafkaService.listKafkaConsumers();
        for (KafkaConsumerInfo kafkaConsumerInfo : kafkaConsumerInfos) {
            if (kafkaConsumerInfo.getActiveTopicNames().contains(topicName)) {
                throw new BusinessException(ResultCode.TOPIC_IS_RUNNING);
            }
        }
        kafkaService.deleteTopics(topicName);
    }

    public enum SearchType {
        LIKE,
        EQUALS;
    }


}
