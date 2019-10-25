package com.pegasus.kafka.service.property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class KafkaMonitorProperty {
    @Value("${zookeeper.connect}")
    private String zookeeper;
}
