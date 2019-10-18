package com.pegasus.kafka.service.kafka;

import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.entity.vo.KafkaConsumerInfo;
import com.pegasus.kafka.entity.vo.OffsetInfo;
import com.pegasus.kafka.service.core.KafkaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaConsumerService {
    private final KafkaService kafkaService;

    public KafkaConsumerService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public List<KafkaConsumerInfo> listKafkaConsumers(String searchGroupId) throws Exception {
        List<KafkaConsumerInfo> kafkaConsumerInfos = kafkaService.listKafkaConsumers(searchGroupId);
        return kafkaConsumerInfos;
    }

    public List<KafkaConsumerInfo> listKafkaConsumers() throws Exception {
        return listKafkaConsumers(null);
    }

    public List<OffsetInfo> listOffsetInfo(String groupId, String topicName) throws Exception {
        return kafkaService.listOffsetInfo(groupId, topicName);
    }

    public void delete(String consumerGroupId) throws Exception {
        try {
            kafkaService.deleteConsumerGroups(consumerGroupId);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.CONSUMER_IS_RUNNING);
        }
    }
}
