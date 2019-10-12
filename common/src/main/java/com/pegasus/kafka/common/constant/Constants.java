package com.pegasus.kafka.common.constant;


public class Constants {
    public static final String BROKER_IDS_PATH = "/brokers/ids";
    public static final String BROKERS_TOPICS_PATH = "/brokers/topics";
    /**
     * topic的partion路径，参数1: topicName
     */
    public static final String BROKERS_TOPICS_PARTITION_PATH = Constants.BROKERS_TOPICS_PATH + "/%s/partitions";
}
