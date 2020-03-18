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
public class PropertyService {
    @Value("${zookeeper.connect}")
    private String zookeeper;

    @Value("${database.name}")
    private String dbName;

    @Value("${database.host}")
    private String dbHost;

    @Value("${database.port}")
    private Integer dbPort;

    @Value("${database.username}")
    private String dbUsername;

    @Value("${database.password}")
    private String dbPassword;

    @Value("${database.retention.days}")
    private Integer dbRetentionDays;

    @Value("${topic.blacklist}")
    private String topicBlackList;
}
