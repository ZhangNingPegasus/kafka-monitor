package com.pegasus.kafka.service.property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The service for reading the configuration values.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Data
@Service
public class KafkaMonitorProperty {
    @Value("${zookeeper.connect}")
    private String zookeeper;
}
