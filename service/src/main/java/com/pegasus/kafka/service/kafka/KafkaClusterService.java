package com.pegasus.kafka.service.kafka;

import com.pegasus.kafka.entity.vo.BrokerVo;
import com.pegasus.kafka.service.core.KafkaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaClusterService {
    private final KafkaService kafkaService;

    public KafkaClusterService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public List<BrokerVo> getAllBrokers() throws Exception {
        return kafkaService.listBrokerInfos();
    }

}