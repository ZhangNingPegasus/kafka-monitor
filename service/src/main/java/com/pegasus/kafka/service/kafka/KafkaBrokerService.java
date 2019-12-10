package com.pegasus.kafka.service.kafka;

import com.pegasus.kafka.common.constant.JMX;
import com.pegasus.kafka.entity.vo.KafkaBrokerVo;
import com.pegasus.kafka.service.core.KafkaJmxService;
import com.pegasus.kafka.service.core.KafkaService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service for Kafka Broker.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class KafkaBrokerService {
    private final KafkaService kafkaService;
    private final KafkaJmxService kafkaJmxService;

    public KafkaBrokerService(KafkaService kafkaService, KafkaJmxService kafkaJmxService) {
        this.kafkaService = kafkaService;
        this.kafkaJmxService = kafkaJmxService;
    }

    public List<KafkaBrokerVo> listAllBrokers() throws Exception {
        List<KafkaBrokerVo> result = kafkaService.listBrokerInfos();
        for (KafkaBrokerVo kafkaBrokerVo : result) {
            String[] data = kafkaJmxService.getData(kafkaBrokerVo, new String[]{JMX.OPERATING_SYSTEM, JMX.OPERATING_SYSTEM}, new String[]{JMX.NAME, JMX.VERSION});
            String name = data[0];
            String version = data[1];
            kafkaBrokerVo.setHost(String.format("%S (%s, %s)", kafkaBrokerVo.getHost(), name, version));
        }
        return result;
    }
}