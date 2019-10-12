package com.pegasus.kafka.service.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KafkaMonitorProperty {

    @Value("${kafka.monitor.zookeeper}")
    private String zookeeper;

    public String getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(String zookeeper) {
        this.zookeeper = zookeeper;
    }
}
