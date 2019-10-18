package com.pegasus.kafka.common.constant;


public class Constants {
    public static final String SESSION_KAFKA_CONSUMER_INFO = "SESSION_KAFKA_CONSUMER_INFO";

    public static final String ZK_BROKER_IDS_PATH = "/brokers/ids";
    public static final String ZK_BROKERS_TOPICS_PATH = "/brokers/topics";

    /**
     * topic的partion路径，参数1: topicName
     */
    public static final String ZK_BROKERS_TOPICS_PARTITION_PATH = Constants.ZK_BROKERS_TOPICS_PATH + "/%s/partitions";

    public static final String KAFKA_MONITOR_SYSTEM_PREFIX = "KAFKA_MONITOR_SYSTEM_";
    public static final String KAFKA_MONITOR_SYSTEM_GROUP_NAME = KAFKA_MONITOR_SYSTEM_PREFIX + "CONSUMER_GROUP_NAME";


}
