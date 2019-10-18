package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.entity.vo.*;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class KafkaService {
    private final KafkaZkService kafkaZkService;
    private final KafkaJmxService kafkaJmxService;


    public KafkaService(KafkaZkService kafkaZkService, KafkaJmxService kafkaJmxService) {
        this.kafkaZkService = kafkaZkService;
        this.kafkaJmxService = kafkaJmxService;
    }

    public void createTopics(String topicName, Integer partitionNumber, Integer replicationNumber) throws Exception {
        kafkaAdminClientDo(adminClient -> {
            NewTopic newTopic = new NewTopic(topicName, partitionNumber, Short.parseShort(replicationNumber.toString()));
            CreateTopicsResult topics = adminClient.createTopics(Arrays.asList(newTopic));
            topics.all().get();
        });
    }

    public void alterTopics(String topicName, Integer partitionNum) throws Exception {
        kafkaAdminClientDo(new KafkaAdminClientAction() {
            @Override
            public void action(KafkaAdminClient adminClient) throws Exception {
                Map<String, NewPartitions> newPartitionsMap = new HashMap<>();
                newPartitionsMap.put(topicName, NewPartitions.increaseTo(partitionNum));
                adminClient.createPartitions(newPartitionsMap);
            }
        });
    }


    public void deleteTopics(String topicName) throws Exception {
        kafkaAdminClientDo(adminClient -> {
            DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Arrays.asList(topicName));
            deleteTopicsResult.all().get();
        });
    }


    public List<KafkaConsumerInfo> listKafkaConsumers(String searchGroupId) throws Exception {
        List<KafkaConsumerInfo> result = new ArrayList<>();
        kafkaAdminClientDo(adminClient -> {
            ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.all().get();
            for (ConsumerGroupListing consumerGroupListing : consumerGroupListings) {
                if (!StringUtils.isEmpty(searchGroupId) && !consumerGroupListing.groupId().equals(searchGroupId)) {
                    continue;
                }
                Set<String> hasOwnerTopics = new HashSet<>();
                String groupId = consumerGroupListing.groupId();
                if (!groupId.startsWith(Constants.KAFKA_MONITOR_SYSTEM_PREFIX)) {
                    DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Arrays.asList(groupId));

                    Node coordinator = describeConsumerGroupsResult.all().get().get(groupId).coordinator();
                    Collection<MemberDescription> members = describeConsumerGroupsResult.describedGroups().get(groupId).get().members();

                    KafkaConsumerInfo kafkaConsumerInfo = new KafkaConsumerInfo();
                    kafkaConsumerInfo.setGroupId(groupId);
                    kafkaConsumerInfo.setNode(String.format("%s : %s", coordinator.host(), coordinator.port()));

                    List<KafkaConsumerInfo.Meta> metaList = new ArrayList<>();
                    for (MemberDescription member : members) {
                        KafkaConsumerInfo.Meta meta = new KafkaConsumerInfo.Meta();
                        meta.setConsumerId(member.consumerId());
                        meta.setNode(member.host().replaceAll("/", ""));

                        List<KafkaConsumerInfo.TopicSubscriber> topicSubscriberList = new ArrayList<>();
                        for (TopicPartition topicPartition : member.assignment().topicPartitions()) {
                            KafkaConsumerInfo.TopicSubscriber topicSubscriber = new KafkaConsumerInfo.TopicSubscriber();
                            topicSubscriber.setTopicName(topicPartition.topic());
                            topicSubscriber.setPartitionId(topicPartition.partition());
                            topicSubscriberList.add(topicSubscriber);
                            hasOwnerTopics.add(topicPartition.topic());
                        }
                        meta.setTopicSubscriberList(topicSubscriberList);
                        metaList.add(meta);
                    }


                    KafkaConsumerInfo.Meta noActiveMeta = new KafkaConsumerInfo.Meta();
                    List<KafkaConsumerInfo.TopicSubscriber> noActivetopicSubscriberList = new ArrayList<>();
                    noActiveMeta.setConsumerId("");
                    noActiveMeta.setNode(" - ");
                    ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = adminClient.listConsumerGroupOffsets(groupId);
                    for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get().entrySet()) {
                        KafkaConsumerInfo.TopicSubscriber topicSubscriber = new KafkaConsumerInfo.TopicSubscriber();
                        topicSubscriber.setTopicName(entry.getKey().topic());
                        topicSubscriber.setPartitionId(entry.getKey().partition());
                        if (!hasOwnerTopics.contains(entry.getKey().topic())) {
                            noActivetopicSubscriberList.add(topicSubscriber);
                        }
                    }
                    noActiveMeta.setTopicSubscriberList(noActivetopicSubscriberList);
                    if (noActiveMeta.getTopicSubscriberList().size() > 0) {
                        metaList.add(noActiveMeta);
                    }

                    kafkaConsumerInfo.setMetaList(metaList);
                    result.add(kafkaConsumerInfo);
                }
            }
        });


        for (KafkaConsumerInfo kafkaConsumerInfo : result) {
            Set<String> topicNameSet = new HashSet<>();
            Set<String> activeTopicSet = new HashSet<>();
            for (KafkaConsumerInfo.Meta meta : kafkaConsumerInfo.getMetaList()) {
                for (KafkaConsumerInfo.TopicSubscriber topicSubscriber : meta.getTopicSubscriberList()) {
                    topicNameSet.add(topicSubscriber.getTopicName());
                    if (!StringUtils.isEmpty(meta.getConsumerId())) {
                        activeTopicSet.add(topicSubscriber.getTopicName());
                    }
                }
            }

            kafkaConsumerInfo.setTopicNames(topicNameSet);
            kafkaConsumerInfo.setActiveTopicNames(activeTopicSet);
            Set<String> notActiveTopicNames = new HashSet<>(topicNameSet.size());
            notActiveTopicNames.addAll(topicNameSet);
            notActiveTopicNames.removeAll(activeTopicSet);
            kafkaConsumerInfo.setNotActiveTopicNames(notActiveTopicNames);

            kafkaConsumerInfo.setActiveTopicCount(activeTopicSet.size());
            kafkaConsumerInfo.setTopicCount(topicNameSet.size());
        }


        return result;
    }

    public List<KafkaConsumerInfo> listKafkaConsumers() throws Exception {
        return listKafkaConsumers(null);
    }

    public List<OffsetInfo> listOffsetInfo(String groupId, String topicName) throws Exception {
        List<OffsetInfo> result = new ArrayList<>();

        List<String> partitionIds = this.listPartitionIds(topicName);
        Map<Integer, Long> partitionOffset = this.listOffset(groupId, topicName, partitionIds);
        Map<TopicPartition, Long> partitionLogSize = this.listLogSize(topicName, partitionIds);

        if (partitionOffset != null && partitionLogSize != null) {
            List<KafkaConsumerInfo> kafkaConsumerInfos = listKafkaConsumers(groupId);
            for (Map.Entry<TopicPartition, Long> entrySet : partitionLogSize.entrySet()) {
                int partitionId = entrySet.getKey().partition();
                long logSize = entrySet.getValue();

                OffsetInfo offsetInfo = new OffsetInfo();
                offsetInfo.setPartitionId(partitionId);
                offsetInfo.setLogSize(logSize);
                if (partitionOffset.containsKey(partitionId)) {
                    offsetInfo.setOffset(partitionOffset.get(partitionId));
                    offsetInfo.setLag(offsetInfo.getOffset() == -1 ? 0 : (offsetInfo.getLogSize() - offsetInfo.getOffset()));
                } else {
                    offsetInfo.setOffset(null);
                    offsetInfo.setLag(null);
                }

                offsetInfo.setConsumerId(getConsumerId(kafkaConsumerInfos, topicName, partitionId));
                result.add(offsetInfo);
            }
        }
        return result;
    }

    private String getConsumerId(List<KafkaConsumerInfo> kafkaConsumerInfoList, String topicName, Integer partitionId) {
        if (kafkaConsumerInfoList != null) {
            for (KafkaConsumerInfo kafkaConsumerInfo : kafkaConsumerInfoList) {
                for (KafkaConsumerInfo.Meta meta : kafkaConsumerInfo.getMetaList()) {
                    for (KafkaConsumerInfo.TopicSubscriber topicSubscriber : meta.getTopicSubscriberList()) {
                        if (topicSubscriber.getTopicName().equals(topicName) && topicSubscriber.getPartitionId().equals(partitionId)) {
                            return meta.getConsumerId();
                        }
                    }
                }
            }
        }
        return null;
    }

    private Map<Integer, Long> listOffset(String groupId, String topicName, List<String> partitionIds) throws Exception {
        Map<Integer, Long> partitionOffset = new HashMap<>();
        kafkaAdminClientDo(adminClient -> {
            List<TopicPartition> tps = new ArrayList<>();
            for (String partitionId : partitionIds) {
                TopicPartition tp = new TopicPartition(topicName, Integer.parseInt(partitionId));
                tps.add(tp);
            }
            ListConsumerGroupOffsetsOptions listConsumerGroupOffsetsOptions = new ListConsumerGroupOffsetsOptions();
            listConsumerGroupOffsetsOptions.topicPartitions();
            ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = adminClient.listConsumerGroupOffsets(groupId, listConsumerGroupOffsetsOptions);
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get().entrySet()) {
                if (topicName.equals(entry.getKey().topic())) {
                    partitionOffset.put(entry.getKey().partition(), entry.getValue().offset());
                }
            }
        });
        return partitionOffset;
    }

    private Map<TopicPartition, Long> listLogSize(String topicName, List<String> partitionIds) throws Exception {
        Map<TopicPartition, Long> result = new HashMap<>();
        kafkaConsumerDo(kafkaConsumer -> {
            Set<TopicPartition> tps = new HashSet<>();
            for (String partitionId : partitionIds) {
                TopicPartition tp = new TopicPartition(topicName, Integer.parseInt(partitionId));
                tps.add(tp);
            }
            kafkaConsumer.assign(tps);
            result.putAll(kafkaConsumer.endOffsets(tps));
        });
        return result;
    }

    /**
     * 获取某个主题的所有分区名称
     *
     * @param topicName
     * @return
     * @throws Exception
     */
    public List<String> listPartitionIds(String topicName) throws Exception {
        return kafkaZkService.getChildren(String.format(Constants.ZK_BROKERS_TOPICS_PARTITION_PATH, topicName));
    }


    /**
     * 根据组名称删除指定消费组
     *
     * @param consumerGroupdId
     * @throws Exception
     */
    public void deleteConsumerGroups(String consumerGroupdId) throws Exception {
        kafkaAdminClientDo(adminClient ->
                adminClient.deleteConsumerGroups(Arrays.asList(consumerGroupdId)).all().get());
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 获取集群中所有的broker信息
     *
     * @return
     * @throws Exception
     */
    public List<BrokerVo> listBrokerInfos() throws Exception {
        List<BrokerVo> brokerInfoList = new ArrayList<>();
        List<String> brokerList = this.listBrokerNames();
        for (String brokerName : brokerList) {
            BrokerVo brokerInfo = this.getBrokerInfo(brokerName);
            brokerInfo.setName(brokerName);
            brokerInfo.setVersion(getKafkaVersion(brokerInfo));
            brokerInfoList.add(brokerInfo);
        }
        return brokerInfoList;
    }

    public List<ConsumerVo> listConsumers() throws Exception {
        List<ConsumerVo> result = new ArrayList<>();
        kafkaAdminClientDo(kafkaAdminClient -> {
            ListConsumerGroupsResult listConsumerGroupsResult = kafkaAdminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.all().get();
            for (ConsumerGroupListing consumerGroupListing : consumerGroupListings) {

                String groupId = consumerGroupListing.groupId();
                DescribeConsumerGroupsResult describeConsumerGroupsResult = kafkaAdminClient.describeConsumerGroups(Arrays.asList(groupId));
                Node coordinator = describeConsumerGroupsResult.all().get().get(groupId).coordinator();
                Collection<MemberDescription> members = describeConsumerGroupsResult.all().get().get(groupId).members();

                for (MemberDescription member : members) {
                    for (TopicPartition topicPartition : member.assignment().topicPartitions()) {
                        ConsumerVo consumerVo = new ConsumerVo();
                        consumerVo.setName(consumerGroupListing.groupId());
                        consumerVo.setNode(String.format("%s:%s", coordinator.host(), coordinator.port()));
                        consumerVo.setMemberId(member.consumerId());
                        consumerVo.setHost(member.host().replace("/", ""));
                        consumerVo.setTopicName(topicPartition.topic());
                        consumerVo.setPartitionId(String.valueOf(topicPartition.partition()));
                        result.add(consumerVo);
                    }
                }

            }
        });
        return result;
    }

    public Long getLag(String topicName) throws Exception {
        Long result = 0L;
        List<ConsumerVo> consumerVoList = listConsumers();
        List<ConsumerVo> consumerVoFilterList = consumerVoList.stream().filter(p -> p.getTopicName().equals(topicName)).collect(Collectors.toList());
        for (ConsumerVo consumerVo : consumerVoFilterList) {
            result += getLag(consumerVo.getName(), topicName);
        }
        return result;
    }

    public Long getLag(String groupName, String topicName) throws Exception {
        final Long[] result = {0L};
        kafkaAdminClientDo(kafkaAdminClient -> {
            ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = kafkaAdminClient.listConsumerGroupOffsets(groupName);
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get().entrySet()) {
                if (topicName.equals(entry.getKey().topic())) {
                    long logSize = getLogSize(entry.getKey().topic(), entry.getKey().partition());
                    result[0] += logSize - entry.getValue().offset();
                }
            }
        });
        return result[0];
    }

    public Long getLogSize(String topicName) throws Exception {
        Long result = 0L;
        List<String> listPartitionNames = listPartitionIds(topicName);
        for (String listPartitionName : listPartitionNames) {
            result += getLogSize(topicName, Integer.parseInt(listPartitionName));
        }
        return result;
    }

    public Long getLogSize(String topicName, int partitionId) throws Exception {
        AtomicLong result = new AtomicLong(0L);
        kafkaConsumerDo(kafkaConsumer -> {
            TopicPartition topicPartition = new TopicPartition(topicName, partitionId);
            kafkaConsumer.assign(Collections.singleton(topicPartition));
            Map<TopicPartition, Long> logsize = kafkaConsumer.endOffsets(Collections.singleton(topicPartition));
            result.set(logsize.get(topicPartition).longValue());
        });
        return result.get();
    }

    public List<TopicPartitionVo> listTopicParitions(String topic) {
        final List<TopicPartitionVo> result = new ArrayList<>();
        try {
            kafkaAdminClientDo(kafkaAdminClient -> {
                DescribeTopicsResult describeTopicsResult = kafkaAdminClient.describeTopics(Arrays.asList(topic));
                Map<String, TopicDescription> topicDescriptionMap = describeTopicsResult.all().get();

                for (Map.Entry<String, TopicDescription> pair : topicDescriptionMap.entrySet()) {
                    String topicName = pair.getKey();
                    TopicDescription topicDescription = pair.getValue();
                    for (TopicPartitionInfo partition : topicDescription.partitions()) {

                        TopicPartitionVo topicPartitionVo = new TopicPartitionVo();
                        topicPartitionVo.setTopicName(topicName);
                        topicPartitionVo.setPartitionId(Integer.toString(partition.partition()));
                        topicPartitionVo.setLeader(new PartitionVo(
                                Integer.toString(partition.leader().id()),
                                partition.leader().host(),
                                Integer.toString(partition.leader().port()),
                                partition.leader().rack()
                        ));

                        List<PartitionVo> partitionVoList = new ArrayList<>();
                        for (Node replica : partition.replicas()) {
                            partitionVoList.add(new PartitionVo(
                                    Integer.toString(replica.id()),
                                    replica.host(),
                                    Integer.toString(replica.port()),
                                    replica.rack()
                            ));
                        }
                        topicPartitionVo.setReplicas(partitionVoList);

                        List<PartitionVo> isrList = new ArrayList<>();
                        for (Node isr : partition.isr()) {
                            isrList.add(new PartitionVo(
                                    Integer.toString(isr.id()),
                                    isr.host(),
                                    Integer.toString(isr.port()),
                                    isr.rack()
                            ));
                        }
                        topicPartitionVo.setIsr(isrList);

                        result.add(topicPartitionVo);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取所有主题名称
     *
     * @return
     * @throws Exception
     */
    public List<String> listTopicNames() throws Exception {
        return kafkaZkService.getChildren(Constants.ZK_BROKERS_TOPICS_PATH).stream().filter(p -> !"__consumer_offsets".equals(p)).collect(Collectors.toList());
    }

    /**
     * 获取某个主题的状态，比如创建时间、修改时间等
     *
     * @param topicName
     * @return
     * @throws Exception
     */
    public Stat getTopicStat(String topicName) throws Exception {
        Stat stat = new Stat();
        kafkaZkService.getData(String.format("%s/%s", Constants.ZK_BROKERS_TOPICS_PATH, topicName), stat);
        return stat;
    }


    /**
     * 获取某个broker的信息
     *
     * @param brokerName
     * @return
     * @throws Exception
     */
    public BrokerVo getBrokerInfo(String brokerName) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String brokerInfoJson = kafkaZkService.getData(String.format("%s/%s", Constants.ZK_BROKER_IDS_PATH, brokerName));
        JSONObject jsonObject = new JSONObject(brokerInfoJson);
        BrokerVo brokerVo = new BrokerVo();
        brokerVo.setHost(jsonObject.get("host").toString());
        brokerVo.setPort(jsonObject.get("port").toString());
        brokerVo.setEndpoints(jsonObject.get("endpoints").toString());
        brokerVo.setJmxPort(jsonObject.get("jmx_port").toString());
        brokerVo.setCreateTime(df.format(new Date(jsonObject.getLong("timestamp"))));
        return brokerVo;
    }

    /**
     * 获取集群中所有的broker名称
     *
     * @return
     * @throws Exception
     */
    private List<String> listBrokerNames() throws Exception {
        return kafkaZkService.getChildren(Constants.ZK_BROKER_IDS_PATH);
    }

    /**
     * 获取kafka的版本号
     *
     * @return
     */
    private String getKafkaVersion(BrokerVo brokerInfo) {
        String result = " - ";
        try {
            result = kafkaJmxService.getData(brokerInfo, String.format("kafka.server:type=app-info,id=%s", brokerInfo.getName()), "Version");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getKafkaBrokerServer() throws Exception {
        StringBuilder kafkaUrls = new StringBuilder();
        List<BrokerVo> brokerInfoList = this.listBrokerInfos();
        if (brokerInfoList == null || brokerInfoList.size() < 1) {
            throw new BusinessException(ResultCode.KAFKA_NOT_RUNNING);
        }
        for (BrokerVo brokerInfo : brokerInfoList) {
            kafkaUrls.append(String.format("%s:%s,", brokerInfo.getHost(), brokerInfo.getPort()));
        }
        if (kafkaUrls.length() > 0) {
            kafkaUrls.delete(kafkaUrls.length() - 1, kafkaUrls.length());
        }
        return kafkaUrls.toString();
    }

    private void kafkaAdminClientDo(KafkaAdminClientAction kafkaAdminClientAction) throws Exception {
        KafkaAdminClient kafkaAdminClient = null;
        try {
            Properties properties = new Properties();
            properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer());
            kafkaAdminClient = (KafkaAdminClient) AdminClient.create(properties);
            kafkaAdminClientAction.action(kafkaAdminClient);
        } finally {
            if (kafkaAdminClient != null) {
                kafkaAdminClient.close();
            }
        }
    }

    private void kafkaConsumerDo(KafkaConsumerAction kafkaConsumerAction) throws Exception {
        KafkaConsumer kafkaConsumer = null;
        try {
            Properties props = new Properties();
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka.monitor.system.group");
            props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            kafkaConsumer = new KafkaConsumer<>(props);
            kafkaConsumerAction.action(kafkaConsumer);
        } finally {
            if (kafkaConsumer != null) {
                kafkaConsumer.close();
            }
        }
    }


    private interface KafkaAdminClientAction {
        void action(KafkaAdminClient kafkaAdminClient) throws Exception;
    }

    private interface KafkaConsumerAction {
        void action(KafkaConsumer kafkaConsumer) throws Exception;
    }

//    public Long getLogSize(String topic) throws Exception {
//        Long result = 0L;
//        List<TopicPartitionVo> topicPartitionVoList = listTopicParitions(topic);
//        List<BrokerVo> brokerVoList = listBrokerInfos();
//        for (TopicPartitionVo topicPartitionVo : topicPartitionVoList) {
//            String host = topicPartitionVo.getLeader().getHost();
//            BrokerVo brokerVo = brokerVoList.stream().filter(p -> p.getHost().equals(host)).findFirst().get();
//            result += getLogSize(brokerVo, topic, topicPartitionVo.getPartitionId());
//        }
//        return result;
//    }
//
//    public Long getLogSize(BrokerVo brokerVo, String topic) {
//        Long result = 0L;
//        List<TopicPartitionVo> topicPartitionVoList = listTopicParitions(topic);
//        for (TopicPartitionVo topicPartitionVo : topicPartitionVoList) {
//            if (topicPartitionVo.getLeader().getHost().equals(brokerVo.getHost())) {
//                result += getLogSize(brokerVo, topic, topicPartitionVo.getPartitionId());
//            }
//        }
//        return result;
//    }
//
//    /**
//     * 获取指定broker和指定分区id下的主题消息总量
//     *
//     * @param brokerVo
//     * @param topic
//     * @param partitionId
//     * @return
//     */
//    public Long getLogSize(BrokerVo brokerVo, String topic, String partitionId) {
//        Long result = 0L;
//        try {
//            String[] data = kafkaJmxService.getData(brokerVo,
//                    new String[]{
//                            String.format("kafka.log:type=Log,name=LogEndOffset,topic=%s,partition=%s", topic, partitionId),
//                            String.format("kafka.log:type=Log,name=LogStartOffset,topic=%s,partition=%s", topic, partitionId)
//                    },
//                    new String[]{
//                            "Value",
//                            "Value"
//                    });
//            long logEndOffset = Long.parseLong(data[0]);
//            long logStartOffset = Long.parseLong(data[1]);
//            result = logEndOffset - logStartOffset;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

}
