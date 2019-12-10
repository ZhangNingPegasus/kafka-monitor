package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.constant.JMX;
import com.pegasus.kafka.entity.vo.KafkaBrokerVo;
import com.pegasus.kafka.entity.vo.MBeanVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The service for kafka's MBean.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class MBeanService {

    private final KafkaJmxService kafkaJmxService;

    public MBeanService(KafkaJmxService kafkaJmxService) {
        this.kafkaJmxService = kafkaJmxService;
    }

    public MBeanVo bytesInPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_IN_PER_SEC);
    }

    public MBeanVo bytesInPerSec(KafkaBrokerVo brokerInfo, String topicName) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_IN_PER_SEC + ",topic=" + topicName);
    }

    public MBeanVo bytesOutPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_OUT_PER_SEC);
    }

    public MBeanVo bytesOutPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_OUT_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo bytesRejectedPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_REJECTED_PER_SEC);
    }

    public MBeanVo bytesRejectedPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_REJECTED_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo failedFetchRequestsPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_FETCH_REQUESTS_PER_SEC);
    }

    public MBeanVo failedFetchRequestsPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_FETCH_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo failedProduceRequestsPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_PRODUCE_REQUESTS_PER_SEC);
    }

    public MBeanVo failedProduceRequestsPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_PRODUCE_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo messagesInPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.MESSAGES_IN_PER_SEC);
    }

    public MBeanVo messagesInPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.MESSAGES_IN_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo produceMessageConversionsPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC);
    }

    public MBeanVo produceMessageConversionsPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo totalFetchRequestsPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_FETCH_REQUESTS_PER_SEC);
    }

    public MBeanVo totalFetchRequestsPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_FETCH_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo totalProduceRequestsPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_PRODUCE_REQUESTS_PER_SEC);
    }

    public MBeanVo totalProduceRequestsPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_PRODUCE_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo replicationBytesInPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_IN_PER_SEC);
    }

    public MBeanVo replicationBytesInPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_IN_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo replicationBytesOutPerSec(KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_OUT_PER_SEC);
    }

    public MBeanVo replicationBytesOutPerSec(KafkaBrokerVo brokerInfo, String topic) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_OUT_PER_SEC + ",topic=" + topic);
    }

    public Long getOsTotalMemory(KafkaBrokerVo brokerInfo) throws Exception {
        return Long.parseLong(kafkaJmxService.getData(brokerInfo, JMX.OPERATING_SYSTEM, JMX.TOTAL_PHYSICAL_MEMORY_SIZE));
    }

    public Long getOsFreeMemory(KafkaBrokerVo brokerInfo) throws Exception {
        return Long.parseLong(kafkaJmxService.getData(brokerInfo, JMX.OPERATING_SYSTEM, JMX.FREE_PHYSICAL_MEMORY_SIZE));
    }

    private MBeanVo getMBeanInfo(KafkaBrokerVo brokerInfo, String name) {
        MBeanVo mbeanVo = new MBeanVo();
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

            mbeanVo.setOneMinute(data[0]);
            mbeanVo.setFiveMinute(data[1]);
            mbeanVo.setFifteenMinute(data[2]);
            mbeanVo.setMeanRate(data[3]);

            mbeanVo.setDblOneMinute(Double.parseDouble(data[0]));
            mbeanVo.setDblFiveMinute(Double.parseDouble(data[1]));
            mbeanVo.setDblFifteenMinute(Double.parseDouble(data[2]));
            mbeanVo.setDblMeanRate(Double.parseDouble(data[3]));

        } catch (Exception e) {
            mbeanVo.setFifteenMinute("0.0");
            mbeanVo.setFiveMinute("0.0");
            mbeanVo.setMeanRate("0.0");
            mbeanVo.setOneMinute("0.0");
        }
        return mbeanVo;
    }
}
