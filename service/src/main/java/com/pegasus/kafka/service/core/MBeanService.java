package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.constant.JMX;
import com.pegasus.kafka.entity.vo.KafkaBrokerInfo;
import com.pegasus.kafka.entity.vo.MBeanInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MBeanService {

    private final KafkaJmxService kafkaJmxService;

    public MBeanService(KafkaJmxService kafkaJmxService) {
        this.kafkaJmxService = kafkaJmxService;
    }

    public MBeanInfo bytesInPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_IN_PER_SEC);
    }

    public MBeanInfo bytesInPerSec(KafkaBrokerInfo brokerInfo, String topicName) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_IN_PER_SEC + ",topic=" + topicName);
    }

    public MBeanInfo bytesOutPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_OUT_PER_SEC);
    }

    public MBeanInfo bytesOutPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_OUT_PER_SEC + ",topic=" + topic);
    }

    public MBeanInfo bytesRejectedPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_REJECTED_PER_SEC);
    }

    public MBeanInfo bytesRejectedPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_REJECTED_PER_SEC + ",topic=" + topic);
    }

    public MBeanInfo failedFetchRequestsPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_FETCH_REQUESTS_PER_SEC);
    }

    public MBeanInfo failedFetchRequestsPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_FETCH_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanInfo failedProduceRequestsPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_PRODUCE_REQUESTS_PER_SEC);
    }

    public MBeanInfo failedProduceRequestsPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_PRODUCE_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanInfo messagesInPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.MESSAGES_IN_PER_SEC);
    }

    public MBeanInfo messagesInPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.MESSAGES_IN_PER_SEC + ",topic=" + topic);
    }

    public MBeanInfo produceMessageConversionsPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC);
    }

    public MBeanInfo produceMessageConversionsPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC + ",topic=" + topic);
    }

    public MBeanInfo totalFetchRequestsPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_FETCH_REQUESTS_PER_SEC);
    }

    public MBeanInfo totalFetchRequestsPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_FETCH_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanInfo totalProduceRequestsPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_PRODUCE_REQUESTS_PER_SEC);
    }

    public MBeanInfo totalProduceRequestsPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_PRODUCE_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanInfo replicationBytesInPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_IN_PER_SEC);
    }

    public MBeanInfo replicationBytesInPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_IN_PER_SEC + ",topic=" + topic);
    }

    public MBeanInfo replicationBytesOutPerSec(KafkaBrokerInfo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_OUT_PER_SEC);
    }

    public MBeanInfo replicationBytesOutPerSec(KafkaBrokerInfo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_OUT_PER_SEC + ",topic=" + topic);
    }

    private MBeanInfo getMBeanInfo(KafkaBrokerInfo brokerInfo, String name) {
        MBeanInfo mbeanInfo = new MBeanInfo();
        try {
            List<String> nameList = new ArrayList<>();
            List<String> attributeList = new ArrayList<>();

            nameList.add(name);
            attributeList.add("OneMinuteRate");

            nameList.add(name);
            attributeList.add("FiveMinuteRate");

            nameList.add(name);
            attributeList.add("FifteenMinuteRate");

            nameList.add(name);
            attributeList.add("MeanRate");

            String[] data = kafkaJmxService.getData(brokerInfo, nameList.toArray(new String[]{}), attributeList.toArray(new String[]{}));

            mbeanInfo.setOneMinute(data[0]);
            mbeanInfo.setFiveMinute(data[1]);
            mbeanInfo.setFifteenMinute(data[2]);
            mbeanInfo.setMeanRate(data[3]);

            mbeanInfo.setDblOneMinute(Double.parseDouble(data[0]));
            mbeanInfo.setDblFiveMinute(Double.parseDouble(data[1]));
            mbeanInfo.setDblFifteenMinute(Double.parseDouble(data[2]));
            mbeanInfo.setDblMeanRate(Double.parseDouble(data[3]));

        } catch (Exception e) {
            mbeanInfo.setFifteenMinute("0.0");
            mbeanInfo.setFiveMinute("0.0");
            mbeanInfo.setMeanRate("0.0");
            mbeanInfo.setOneMinute("0.0");
        }
        return mbeanInfo;
    }
}
