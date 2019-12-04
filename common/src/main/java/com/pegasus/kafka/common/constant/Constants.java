package com.pegasus.kafka.common.constant;

import com.pegasus.kafka.common.utils.Common;

import java.util.Arrays;
import java.util.List;

/**
 * Constant variable.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
public class Constants {
    public static final String DATABASE_NAME = "`kafka_monitor`";

    public static final String DEFAULT_ADMIN_USER_NAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = Common.hash("admin");

    public static final String SYSTEM_ROLE_NAME = "超级管理员";

    public static final Integer MAX_PAGE_NUM = 10000;

    public static final String EHCACHE_CONFIG_NAME = "EHCACHE_CONFIG_NAME";
    public static final String EHCACHE_KAFKA_BROKER_SERVER = "EHCACHE_KAFKA_BROKER_SERVER";
    public static final String EHCACHE_KAFKA_BROKER_INFOS = "EHCACHE_KAFKA_BROKER_INFOS";


    public static final String SESSION_KAFKA_CONSUMER_INFO = "SESSION_KAFKA_CONSUMER_INFO";

    public static final String ZK_BROKER_IDS_PATH = "/brokers/ids";
    public static final String ZK_BROKERS_TOPICS_PATH = "/brokers/topics";

    /**
     * the path of topics' partition in ZooKeeper，参数1: topicName
     */
    public static final String ZK_BROKERS_TOPICS_PARTITION_PATH = Constants.ZK_BROKERS_TOPICS_PATH + "/%s/partitions";
    public static final String KAFKA_COMPRESS_TYPE = "lz4";

    public final static List<String> KAFKA_SYSTEM_TOPIC = Arrays.asList("__consumer_offsets", "__transaction_state");
    public static final String KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX = "kafka_monitor_pegasus_system_";
    public static final String KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MONITOR = KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX + "consumer_group_name_for_monitor";
    public static final String KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MESSAGE = KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX + "consumer_group_name_for_message";
    /**
     * the partition [%s] is not avaiable，parameter 1: partition_id
     */
    public static final String PARTITION_NOT_AVAIABLE = "分区[%s]不可用";

    public static final String HOST_NOT_AVAIABLE = "主机不可用，请检查";


    public static final String CURRENT_ADMIN_LOGIN = "CURRENT_ADMIN_LOGIN";
}
