package com.pegasus.kafka.common.constant;


public class JMX {
    public static final String BYTES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec";
    public static final String BYTES_OUT_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec";
    public static final String BYTES_REJECTED_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec";
    public static final String FAILED_FETCH_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec";
    public static final String FAILED_PRODUCE_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec";
    public static final String MESSAGES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
    public static final String PRODUCE_MESSAGE_CONVERSIONS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=ProduceMessageConversionsPerSec";
    public static String TOTAL_FETCH_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=TotalFetchRequestsPerSec";
    public static String TOTAL_PRODUCE_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=TotalProduceRequestsPerSec";
    public static String REPLICATION_BYTES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesInPerSec";
    public static String REPLICATION_BYTES_OUT_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesOutPerSec";


    public final static String MESSAGES_IN = "Messages in /sec";
    public final static String BYTES_IN = "Bytes in /sec";
    public final static String BYTES_OUT = "Bytes out /sec";
    public final static String BYTES_REJECTED = "Bytes rejected /sec";
    public final static String FAILED_FETCH_REQUEST = "Failed fetch request /sec";
    public final static String FAILED_PRODUCE_REQUEST = "Failed produce request /sec";
    public final static String TOTAL_FETCH_REQUESTS = "Total fetch requests /sec";
    public final static String TOTAL_PRODUCE_REQUESTS = "Total produce requests /sec";
    public final static String PRODUCE_MESSAGE_CONVERSIONS = "Produce message conversions /sec";

}
