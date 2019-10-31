package com.pegasus.kafka.service.kafka;

import com.pegasus.kafka.common.ehcache.EhcacheService;
import com.pegasus.kafka.entity.vo.KafkaBrokerInfo;
import com.pegasus.kafka.service.core.KafkaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaBrokerService {
    private final KafkaService kafkaService;
    private final EhcacheService ehcacheService;

    public KafkaBrokerService(KafkaService kafkaService, EhcacheService ehcacheService) {
        this.kafkaService = kafkaService;
        this.ehcacheService = ehcacheService;
    }

    public List<KafkaBrokerInfo> listAllBrokers() throws Exception {
        String key = "KafkaBrokerService::listAllBrokers";
        List<KafkaBrokerInfo> result = ehcacheService.get(key);
        if (result == null) {
            result = kafkaService.listBrokerInfos();
            ehcacheService.set(key, result);
        }
        return result;
    }
}