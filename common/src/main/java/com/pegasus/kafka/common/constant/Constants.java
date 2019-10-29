package com.pegasus.kafka.common.constant;


public class Constants {
    public static final String DATABASE_NAME = "`kafka_monitor`";
    public static final Integer MAX_PAGE_NUM = 10000;

    public static final String EHCACHE_CONFIG_NAME = "EHCACHE_CONFIG_NAME";
    public static final String EHCACHE_KAFKA_BROKER_SERVER = "EHCACHE_KAFKA_BROKER_SERVER";
    public static final String EHCACHE_KAFKA_BROKER_INFOS = "EHCACHE_KAFKA_BROKER_INFOS";


    public static final String SESSION_KAFKA_CONSUMER_INFO = "SESSION_KAFKA_CONSUMER_INFO";

    public static final String ZK_BROKER_IDS_PATH = "/brokers/ids";
    public static final String ZK_BROKERS_TOPICS_PATH = "/brokers/topics";

    /**
     * topic的partion路径，参数1: topicName
     */
    public static final String ZK_BROKERS_TOPICS_PARTITION_PATH = Constants.ZK_BROKERS_TOPICS_PATH + "/%s/partitions";
    public static final String KAFKA_COMPRESS_TYPE = "lz4";

    public static final String KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX = "kafka_monitor_pegasus_system_";
    public static final String KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MONITOR = KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX + "consumer_group_name_for_monitor";
    public static final String KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MESSAGE = KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX + "consumer_group_name_for_message";

    /**
     * 分区[%s]不可用，参数1: partition_id
     */
    public static final String PARTITION_NOT_AVAIABLE = "分区[%s]不可用";

    public static final String HOST_NOT_AVAIABLE = "主机不可用，请检查";


}
