package com.pegasus.kafka.service.kafka;

import com.pegasus.kafka.entity.vo.KafkaBrokerInfo;
import com.pegasus.kafka.service.core.KafkaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaBrokerService {
    private final KafkaService kafkaService;

    public KafkaBrokerService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public List<KafkaBrokerInfo> listAllBrokers() throws Exception {
        return kafkaService.listBrokerInfos();
    }
}