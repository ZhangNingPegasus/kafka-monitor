package com.pegasus.kafka.service.kafka;

import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.entity.vo.KafkaConsumerVo;
import com.pegasus.kafka.entity.vo.OffsetVo;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.dto.SysLagService;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The service for Kafka Consumer.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class KafkaConsumerService {
    private final KafkaService kafkaService;
    private final SysLagService sysLagService;

    public KafkaConsumerService(KafkaService kafkaService, SysLagService sysLagService) {
        this.kafkaService = kafkaService;
        this.sysLagService = sysLagService;
    }

    public List<KafkaConsumerVo> listKafkaConsumers(String searchGroupId) throws Exception {
        return kafkaService.listKafkaConsumers(searchGroupId);
    }

    public List<KafkaConsumerVo> listKafkaConsumers() throws Exception {
        return listKafkaConsumers(null);
    }

    public List<String> listAllConsumers() throws Exception {
        List<String> groupIdList = new ArrayList<>();
        kafkaService.kafkaAdminClientDo(kafkaAdminClient -> {
            ListConsumerGroupsResult listConsumerGroupsResult = kafkaAdminClient.listConsumerGroups();
            for (ConsumerGroupListing consumerGroupListing : listConsumerGroupsResult.all().get()) {
                groupIdList.add(consumerGroupListing.groupId());
            }
        });
        return groupIdList;
    }

    public List<OffsetVo> listOffsetVo(String groupId, String topicName) throws Exception {
        return kafkaService.listOffsetVo(groupId, topicName);
    }

    public void delete(String consumerGroupId) {
        try {
            kafkaService.deleteConsumerGroups(consumerGroupId);
            sysLagService.deleteConsumerName(consumerGroupId);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.CONSUMER_IS_RUNNING);
        }
    }
}
