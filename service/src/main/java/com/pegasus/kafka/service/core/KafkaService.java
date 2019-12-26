package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.constant.JMX;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysKpi;
import com.pegasus.kafka.entity.po.Out;
import com.pegasus.kafka.entity.vo.*;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * The service for kafka.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class KafkaService {
    private final KafkaZkService kafkaZkService;
    private final KafkaJmxService kafkaJmxService;
    private final MBeanService mBeanService;
    private final MBeanService mbeanService;

    public KafkaService(KafkaZkService kafkaZkService, KafkaJmxService kafkaJmxService, MBeanService mBeanService, MBeanService mbeanService) {
        this.kafkaZkService = kafkaZkService;
        this.kafkaJmxService = kafkaJmxService;
        this.mBeanService = mBeanService;
        this.mbeanService = mbeanService;
    }

    public void createTopics(String topicName, Integer partitionNumber, Integer replicationNumber) throws Exception {
        kafkaAdminClientDo(adminClient -> {
            NewTopic newTopic = new NewTopic(topicName, partitionNumber, Short.parseShort(replicationNumber.toString()));
            CreateTopicsResult topics = adminClient.createTopics(Collections.singletonList(newTopic));
            topics.all().get();
        });
    }

    public void sendMessage(String topicName, String key, String value) throws Exception {
        kafkaProducerDo(kafkaProducer -> kafkaProducer.send(new ProducerRecord(topicName, key, value), (metadata, exception) -> {
            if (exception != null) {
                throw new BusinessException(exception);
            }
        }));
    }

    public void sendMessage(String topicName, String value) throws Exception {
        sendMessage(topicName, null, value);
    }

    public void alterTopics(String topicName, Integer partitionNum) throws Exception {
        kafkaAdminClientDo(adminClient -> {
            Map<String, NewPartitions> newPartitionsMap = new HashMap<>();
            newPartitionsMap.put(topicName, NewPartitions.increaseTo(partitionNum));
            adminClient.createPartitions(newPartitionsMap);
        });
    }

    public void deleteTopic(String topicName) throws Exception {
        kafkaAdminClientDo(adminClient -> {
            DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Collections.singletonList(topicName));
            deleteTopicsResult.all().get();
        });
    }

    public List<KafkaTopicPartitionVo> listTopicDetails(String topicName, boolean needLogsize) throws Exception {
        final List<KafkaTopicPartitionVo> result = new ArrayList<>();
        AtomicReference<DescribeTopicsResult> describeTopicsResult = new AtomicReference<>();

        kafkaAdminClientDo(kafkaAdminClient -> describeTopicsResult.set(kafkaAdminClient.describeTopics(Collections.singletonList(topicName))));

        Map<String, TopicDescription> topicDescriptionMap = describeTopicsResult.get().all().get();

        for (Map.Entry<String, TopicDescription> pair : topicDescriptionMap.entrySet()) {
            TopicDescription topicDescription = pair.getValue();
            for (TopicPartitionInfo partition : topicDescription.partitions()) {
                KafkaTopicPartitionVo topicPartitionVo = new KafkaTopicPartitionVo();
                topicPartitionVo.setTopicName(pair.getKey());
                topicPartitionVo.setPartitionId(Integer.toString(partition.partition()));
                if (partition.leader() == null) {
                    topicPartitionVo.setLogsize(-1L);
                } else {
                    topicPartitionVo.setLeader(new KafkaTopicPartitionVo.PartionInfo(
                            Integer.toString(partition.leader().id()),
                            partition.leader().host(),
                            Integer.toString(partition.leader().port()),
                            partition.leader().rack()
                    ));

                    List<KafkaTopicPartitionVo.PartionInfo> partitionVoList = new ArrayList<>();
                    for (Node replica : partition.replicas()) {
                        partitionVoList.add(new KafkaTopicPartitionVo.PartionInfo(
                                Integer.toString(replica.id()),
                                replica.host(),
                                Integer.toString(replica.port()),
                                replica.rack()
                        ));
                    }
                    topicPartitionVo.setReplicas(partitionVoList);

                    List<KafkaTopicPartitionVo.PartionInfo> isrList = new ArrayList<>();
                    for (Node isr : partition.isr()) {
                        isrList.add(new KafkaTopicPartitionVo.PartionInfo(
                                Integer.toString(isr.id()),
                                isr.host(),
                                Integer.toString(isr.port()),
                                isr.rack()
                        ));
                    }
                    topicPartitionVo.setIsr(isrList);
                }
                result.add(topicPartitionVo);
            }
        }

        if (needLogsize) {
            kafkaConsumerDo(kafkaConsumer -> {
                for (KafkaTopicPartitionVo topicPartitionVo : result) {
                    if (topicPartitionVo.getLeader() != null) {
                        topicPartitionVo.setLogsize(listLogSize(kafkaConsumer, topicPartitionVo.getTopicName(), Integer.valueOf(topicPartitionVo.getPartitionId())));
                    }
                }
            });
        }
        return result;
    }

    private Long listLogSize(KafkaConsumer kafkaConsumer, String topicName, Integer partitionId) {
        TopicPartition tp = new TopicPartition(topicName, partitionId);
        kafkaConsumer.assign(Collections.singleton(tp));
        Map<TopicPartition, Long> endLogSize = kafkaConsumer.endOffsets(Collections.singleton(tp));
//        Map<TopicPartition, Long> startLogSize = kafkaConsumer.beginningOffsets(Collections.singleton(tp));
//        return endLogSize.get(tp) - startLogSize.get(tp);
        return endLogSize.get(tp);
    }

    public List<String> listAllConsumers() throws Exception {
        AtomicReference<List<String>> result = new AtomicReference<>(new ArrayList<>(1024));
        kafkaAdminClientDo(adminClient -> {
            ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.all().get();
            for (ConsumerGroupListing consumerGroupListing : consumerGroupListings) {
                result.get().add(consumerGroupListing.groupId());
            }
        });
        return result.get();
    }

    public List<KafkaConsumerVo> listKafkaConsumers(String searchGroupId) throws Exception {
        List<KafkaConsumerVo> result = new ArrayList<>();
        kafkaAdminClientDo(adminClient -> {
            ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.all().get();
            for (ConsumerGroupListing consumerGroupListing : consumerGroupListings) {
                if (!StringUtils.isEmpty(searchGroupId) && !consumerGroupListing.groupId().equals(searchGroupId)) {
                    continue;
                }
                Set<String> hasOwnerTopics = new HashSet<>();
                String groupId = consumerGroupListing.groupId();
                if (!groupId.startsWith(Constants.KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX)) {
                    DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Collections.singletonList(groupId));

                    Node coordinator = describeConsumerGroupsResult.all().get().get(groupId).coordinator();
                    Collection<MemberDescription> members = describeConsumerGroupsResult.describedGroups().get(groupId).get().members();

                    KafkaConsumerVo kafkaConsumerVo = new KafkaConsumerVo();
                    kafkaConsumerVo.setGroupId(groupId);
                    kafkaConsumerVo.setNode(String.format("%s : %s", coordinator.host(), coordinator.port()));

                    List<KafkaConsumerVo.Meta> metaList = new ArrayList<>();
                    for (MemberDescription member : members) {
                        KafkaConsumerVo.Meta meta = new KafkaConsumerVo.Meta();
                        meta.setConsumerId(member.consumerId());
                        meta.setNode(member.host().replaceAll("/", ""));

                        List<KafkaConsumerVo.TopicSubscriber> topicSubscriberList = new ArrayList<>();
                        for (TopicPartition topicPartition : member.assignment().topicPartitions()) {
                            KafkaConsumerVo.TopicSubscriber topicSubscriber = new KafkaConsumerVo.TopicSubscriber();
                            topicSubscriber.setTopicName(topicPartition.topic());
                            topicSubscriber.setPartitionId(topicPartition.partition());
                            topicSubscriberList.add(topicSubscriber);
                            hasOwnerTopics.add(topicPartition.topic());
                        }
                        meta.setTopicSubscriberList(topicSubscriberList);
                        metaList.add(meta);
                    }
                    KafkaConsumerVo.Meta noActiveMeta = new KafkaConsumerVo.Meta();
                    List<KafkaConsumerVo.TopicSubscriber> noActivetopicSubscriberList = new ArrayList<>();
                    noActiveMeta.setConsumerId("");
                    noActiveMeta.setNode(" - ");
                    ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = adminClient.listConsumerGroupOffsets(groupId);
                    for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get().entrySet()) {
                        KafkaConsumerVo.TopicSubscriber topicSubscriber = new KafkaConsumerVo.TopicSubscriber();
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

                    kafkaConsumerVo.setMetaList(metaList);
                    result.add(kafkaConsumerVo);
                }
            }
        });

        for (KafkaConsumerVo kafkaConsumerVo : result) {
            Set<String> topicNameSet = new HashSet<>();
            Set<String> activeTopicSet = new HashSet<>();
            for (KafkaConsumerVo.Meta meta : kafkaConsumerVo.getMetaList()) {
                for (KafkaConsumerVo.TopicSubscriber topicSubscriber : meta.getTopicSubscriberList()) {
                    topicNameSet.add(topicSubscriber.getTopicName());
                    if (!StringUtils.isEmpty(meta.getConsumerId())) {
                        //TODO : need fix
                        activeTopicSet.add(topicSubscriber.getTopicName());
                    }
                }
            }

            kafkaConsumerVo.setTopicNames(topicNameSet);
            kafkaConsumerVo.setActiveTopicNames(activeTopicSet);
            Set<String> notActiveTopicNames = new HashSet<>(topicNameSet.size());
            notActiveTopicNames.addAll(topicNameSet);
            notActiveTopicNames.removeAll(activeTopicSet);
            kafkaConsumerVo.setNotActiveTopicNames(notActiveTopicNames);
            kafkaConsumerVo.setActiveTopicCount(activeTopicSet.size());
            kafkaConsumerVo.setTopicCount(topicNameSet.size());
        }

        result.sort(Comparator.comparing(KafkaConsumerVo::getGroupId));
        return result;
    }

    public List<KafkaConsumerVo> listKafkaConsumers() throws Exception {
        return listKafkaConsumers(null);
    }

    public List<OffsetVo> listOffsetVo(String groupId, String topicName) throws Exception {
        List<OffsetVo> result = new ArrayList<>();

        List<String> partitionIds = this.listPartitionIds(topicName);
        Map<Integer, Long> partitionOffset = this.listOffset(groupId, topicName, partitionIds);
        Map<TopicPartition, Long> partitionLogSize = this.listLogSize(topicName, partitionIds);

        List<KafkaConsumerVo> kafkaConsumerVoList = listKafkaConsumers(groupId);
        for (Map.Entry<TopicPartition, Long> entrySet : partitionLogSize.entrySet()) {
            int partitionId = entrySet.getKey().partition();
            long logSize = entrySet.getValue();

            OffsetVo offsetVo = new OffsetVo();
            offsetVo.setPartitionId(partitionId);
            offsetVo.setLogSize(logSize);
            if (partitionOffset.containsKey(partitionId)) {
                offsetVo.setOffset(partitionOffset.get(partitionId));
                offsetVo.setLag(offsetVo.getOffset() == -1 ? 0 : (offsetVo.getLogSize() - offsetVo.getOffset()));
            } else {
                offsetVo.setOffset(null);
                offsetVo.setLag(null);
            }

            offsetVo.setConsumerId(getConsumerId(kafkaConsumerVoList, topicName, partitionId));
            result.add(offsetVo);
        }

        if (result.size() < partitionOffset.size()) {
            for (OffsetVo offsetVo : result) {
                partitionOffset.remove(offsetVo.getPartitionId());
            }

            for (Map.Entry<Integer, Long> entry : partitionOffset.entrySet()) {
                OffsetVo offsetVo = new OffsetVo();
                offsetVo.setPartitionId(entry.getKey());
                offsetVo.setOffset(entry.getValue());
                offsetVo.setLogSize(-1L);
                offsetVo.setLag(-1L);
                offsetVo.setConsumerId(String.format(Constants.PARTITION_NOT_AVAIABLE, entry.getKey()));
                result.add(offsetVo);
            }
        }
        return result;
    }

    private String getConsumerId(List<KafkaConsumerVo> kafkaConsumerVoList, String topicName, Integer partitionId) {
        if (kafkaConsumerVoList == null) {
            return null;
        }
        for (KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
            for (KafkaConsumerVo.Meta meta : kafkaConsumerVo.getMetaList()) {
                for (KafkaConsumerVo.TopicSubscriber topicSubscriber : meta.getTopicSubscriberList()) {
                    if (topicSubscriber.getTopicName().equals(topicName) && topicSubscriber.getPartitionId().equals(partitionId)) {
                        return meta.getConsumerId();
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
        List<KafkaTopicPartitionVo> topicDetails = this.listTopicDetails(topicName, false);

        kafkaConsumerDo(kafkaConsumer -> {
            Set<TopicPartition> tps = new HashSet<>();
            for (String partitionId : partitionIds) {
                Optional<KafkaTopicPartitionVo> first = topicDetails.stream().filter(p -> p.getPartitionId().equals(partitionId)).findFirst();
                if (!first.isPresent()) {
                    continue;
                }
                KafkaTopicPartitionVo kafkaTopicPartitionVo = first.get();
                if (kafkaTopicPartitionVo.getLeader() == null) {
                    continue;
                }
                TopicPartition tp = new TopicPartition(topicName, Integer.parseInt(partitionId));
                tps.add(tp);
            }
            kafkaConsumer.assign(tps);
            result.putAll(kafkaConsumer.endOffsets(tps));
        });
        return result;
    }


    public List<String> listPartitionIds(String topicName) throws Exception {
        return kafkaZkService.getChildren(String.format(Constants.ZK_BROKERS_TOPICS_PARTITION_PATH, topicName));
    }

    public void deleteConsumerGroups(String consumerGroupdId) throws Exception {
        kafkaAdminClientDo(adminClient -> adminClient.deleteConsumerGroups(Collections.singletonList(consumerGroupdId)).all().get());
    }

    public String listTopicSize(String topicName) throws Exception {
        long totalSize = 0L;
        List<KafkaTopicPartitionVo> topicDetails = listTopicDetails(topicName, false);
        List<KafkaBrokerVo> brokerVoList = listBrokerInfos();

        for (KafkaTopicPartitionVo topicDetail : topicDetails) {
            if (topicDetail.getLeader() == null) {
                continue;
            }
            KafkaTopicPartitionVo.PartionInfo leader = topicDetail.getLeader();
            Optional<KafkaBrokerVo> first = brokerVoList.stream().filter(p -> p.getHost().equals(leader.getHost())).findFirst();
            if (first.isPresent()) {
                KafkaBrokerVo brokerVo = first.get();
                String size = kafkaJmxService.getData(brokerVo, String.format("kafka.log:type=Log,name=Size,topic=%s,partition=%s", topicName, topicDetail.getPartitionId()), "Value");
                totalSize += Long.parseLong(size);
            }
        }
        return Common.convertSize(totalSize);
    }

    public List<KafkaBrokerVo> listBrokerInfos() throws Exception {
        List<String> brokerList = this.listBrokerNames();
        List<KafkaBrokerVo> result = new ArrayList<>(brokerList.size());
        for (String brokerName : brokerList) {
            KafkaBrokerVo brokerInfo = this.getBrokerInfo(brokerName);
            brokerInfo.setName(brokerName);
            brokerInfo.setVersion(getKafkaVersion(brokerInfo));
            result.add(brokerInfo);
        }
        return result;
    }

    public List<MBeanVo> listTopicMBean(String topicName) throws Exception {
        Map<String, MBeanVo> result = new LinkedHashMap<>();
        List<KafkaBrokerVo> brokerInfoList = this.listBrokerInfos();
        for (KafkaBrokerVo brokerInfo : brokerInfoList) {
            MBeanVo bytesIn = mBeanService.bytesInPerSec(brokerInfo, topicName);
            MBeanVo bytesOut = mBeanService.bytesOutPerSec(brokerInfo, topicName);
            MBeanVo bytesRejected = mBeanService.bytesRejectedPerSec(brokerInfo, topicName);
            MBeanVo failedFetchRequest = mBeanService.failedFetchRequestsPerSec(brokerInfo, topicName);
            MBeanVo failedProduceRequest = mBeanService.failedProduceRequestsPerSec(brokerInfo, topicName);
            MBeanVo messageIn = mBeanService.messagesInPerSec(brokerInfo, topicName);
            MBeanVo produceMessageConversions = mBeanService.produceMessageConversionsPerSec(brokerInfo, topicName);
            MBeanVo totalFetchRequests = mBeanService.totalFetchRequestsPerSec(brokerInfo, topicName);
            MBeanVo totalProduceRequests = mBeanService.totalProduceRequestsPerSec(brokerInfo, topicName);

            assembleMBeanInfo(result, JMX.MESSAGES_IN, messageIn);
            assembleMBeanInfo(result, JMX.BYTES_IN, bytesIn);
            assembleMBeanInfo(result, JMX.BYTES_OUT, bytesOut);
            assembleMBeanInfo(result, JMX.BYTES_REJECTED, bytesRejected);
            assembleMBeanInfo(result, JMX.FAILED_FETCH_REQUEST, failedFetchRequest);
            assembleMBeanInfo(result, JMX.FAILED_PRODUCE_REQUEST, failedProduceRequest);
            assembleMBeanInfo(result, JMX.TOTAL_FETCH_REQUESTS, totalFetchRequests);
            assembleMBeanInfo(result, JMX.TOTAL_PRODUCE_REQUESTS, totalProduceRequests);
            assembleMBeanInfo(result, JMX.PRODUCE_MESSAGE_CONVERSIONS, produceMessageConversions);
        }

        for (Map.Entry<String, MBeanVo> entry : result.entrySet()) {
            if (entry == null || entry.getValue() == null) {
                continue;
            }
            if (entry.getKey().equals(JMX.MESSAGES_IN) || entry.getKey().equals(JMX.BYTES_IN) || entry.getKey().equals(JMX.BYTES_OUT) || entry.getKey().equals(JMX.BYTES_REJECTED)) {
                entry.getValue().setMeanRate(Common.convertSize(entry.getValue().getMeanRate()));
                entry.getValue().setOneMinute(Common.convertSize(entry.getValue().getOneMinute()));
                entry.getValue().setFiveMinute(Common.convertSize(entry.getValue().getFiveMinute()));
                entry.getValue().setFifteenMinute(Common.convertSize(entry.getValue().getFifteenMinute()));
            }
        }
        return new ArrayList<>(result.values());
    }

    private void assembleMBeanInfo(Map<String, MBeanVo> mbeans, String mBeanInfoKey, MBeanVo mBeanVo) {
        if (mbeans.containsKey(mBeanInfoKey) && mBeanVo != null) {
            DecimalFormat formatter = new DecimalFormat("###.##");
            MBeanVo existedMBeanVo = mbeans.get(mBeanInfoKey);
            String fifteenMinuteOld = existedMBeanVo.getFifteenMinute() == null ? "0.0" : existedMBeanVo.getFifteenMinute();
            String fifteenMinuteLastest = mBeanVo.getFifteenMinute() == null ? "0.0" : mBeanVo.getFifteenMinute();
            String fiveMinuteOld = existedMBeanVo.getFiveMinute() == null ? "0.0" : existedMBeanVo.getFiveMinute();
            String fiveMinuteLastest = mBeanVo.getFiveMinute() == null ? "0.0" : mBeanVo.getFiveMinute();
            String meanRateOld = existedMBeanVo.getMeanRate() == null ? "0.0" : existedMBeanVo.getMeanRate();
            String meanRateLastest = mBeanVo.getMeanRate() == null ? "0.0" : mBeanVo.getMeanRate();
            String oneMinuteOld = existedMBeanVo.getOneMinute() == null ? "0.0" : existedMBeanVo.getOneMinute();
            String oneMinuteLastest = mBeanVo.getOneMinute() == null ? "0.0" : mBeanVo.getOneMinute();
            double fifteenMinute = Common.numberic(fifteenMinuteOld) + Common.numberic(fifteenMinuteLastest);
            double fiveMinute = Common.numberic(fiveMinuteOld) + Common.numberic(fiveMinuteLastest);
            double meanRate = Common.numberic(meanRateOld) + Common.numberic(meanRateLastest);
            double oneMinute = Common.numberic(oneMinuteOld) + Common.numberic(oneMinuteLastest);
            existedMBeanVo.setFifteenMinute(formatter.format(fifteenMinute));
            existedMBeanVo.setFiveMinute(formatter.format(fiveMinute));
            existedMBeanVo.setMeanRate(formatter.format(meanRate));
            existedMBeanVo.setOneMinute(formatter.format(oneMinute));
            existedMBeanVo.setName(mBeanInfoKey);
        } else {
            mbeans.put(mBeanInfoKey, mBeanVo);
        }
    }

    public Long getLogSize(String topicName, Out out) throws Exception {
        long result = 0L;
        List<String> listPartitionNames = listPartitionIds(topicName);
        List<KafkaTopicPartitionVo> topicDetails = listTopicDetails(topicName, false);
        for (String listPartitionName : listPartitionNames) {
            Optional<KafkaTopicPartitionVo> first = topicDetails.stream().filter(p -> listPartitionName.equals(p.getPartitionId())).findFirst();
            if (!first.isPresent()) {
                continue;
            }
            KafkaTopicPartitionVo kafkaTopicPartitionVo = first.get();
            if (kafkaTopicPartitionVo.getLeader() == null) {
                if (out != null) {
                    out.setError(String.format(Constants.PARTITION_NOT_AVAIABLE, listPartitionName));
                }
                continue;
            }
            result += getLogSize(topicName, Integer.parseInt(listPartitionName));
        }
        return result;
    }

    private Long getLogSize(String topicName, int partitionId) throws Exception {
        AtomicLong result = new AtomicLong(0L);
        kafkaConsumerDo(kafkaConsumer -> {
            TopicPartition topicPartition = new TopicPartition(topicName, partitionId);
            kafkaConsumer.assign(Collections.singleton(topicPartition));
            Map<TopicPartition, Long> logsize = kafkaConsumer.endOffsets(Collections.singleton(topicPartition));
            result.set(logsize.get(topicPartition));
        });
        return result.get();
    }

    public List<String> listTopicNames() throws Exception {
        return kafkaZkService.getChildren(Constants.ZK_BROKERS_TOPICS_PATH).stream().filter(p -> !Constants.KAFKA_SYSTEM_TOPIC.contains(p)).collect(Collectors.toList());
    }

    public Stat getTopicStat(String topicName) throws Exception {
        Stat stat = new Stat();
        kafkaZkService.getData(String.format("%s/%s", Constants.ZK_BROKERS_TOPICS_PATH, topicName), stat);
        return stat;
    }

    public List<SysKpi> kpi(Date now) throws Exception {
        List<SysKpi> result = new ArrayList<>(SysKpi.KAFKA_KPI.values().length);
        List<KafkaBrokerVo> brokers = this.listBrokerInfos();

        for (SysKpi.KAFKA_KPI kpi : SysKpi.KAFKA_KPI.values()) {
            if (StringUtils.isEmpty(kpi.getName())) {
                continue;
            }
            SysKpi sysKpi = new SysKpi();
            sysKpi.setKpi(kpi.getCode());
            sysKpi.setCreateTime(now);
            StringBuilder host = new StringBuilder();
            for (KafkaBrokerVo broker : brokers) {
                host.append(broker.getHost()).append(",");
                switch (kpi) {
                    case KAFKA_MESSAGES_IN:
                        MBeanVo msg = mbeanService.messagesInPerSec(broker);
                        if (msg != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(msg.getOneMinute()));
                        }
                        break;
                    case KAFKA_BYTES_IN:
                        MBeanVo bin = mbeanService.bytesInPerSec(broker);
                        if (bin != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(bin.getOneMinute()));
                        }
                        break;
                    case KAFKA_BYTES_OUT:
                        MBeanVo bout = mbeanService.bytesOutPerSec(broker);
                        if (bout != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(bout.getOneMinute()));
                        }
                        break;
                    case KAFKA_BYTES_REJECTED:
                        MBeanVo bytesRejectedPerSec = mbeanService.bytesRejectedPerSec(broker);
                        if (bytesRejectedPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(bytesRejectedPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_FAILED_FETCH_REQUEST:
                        MBeanVo failedFetchRequestsPerSec = mbeanService.failedFetchRequestsPerSec(broker);
                        if (failedFetchRequestsPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(failedFetchRequestsPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_FAILED_PRODUCE_REQUEST:
                        MBeanVo failedProduceRequestsPerSec = mbeanService.failedProduceRequestsPerSec(broker);
                        if (failedProduceRequestsPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(failedProduceRequestsPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_TOTAL_FETCH_REQUESTS_PER_SEC:
                        MBeanVo totalFetchRequests = mbeanService.totalFetchRequestsPerSec(broker);
                        if (totalFetchRequests != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(totalFetchRequests.getOneMinute()));
                        }
                        break;
                    case KAFKA_TOTAL_PRODUCE_REQUESTS_PER_SEC:
                        MBeanVo totalProduceRequestsPerSec = mbeanService.totalProduceRequestsPerSec(broker);
                        if (totalProduceRequestsPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(totalProduceRequestsPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_REPLICATION_BYTES_IN_PER_SEC:
                        MBeanVo replicationBytesInPerSec = mbeanService.replicationBytesInPerSec(broker);
                        if (replicationBytesInPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(replicationBytesInPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_REPLICATION_BYTES_OUT_PER_SEC:
                        MBeanVo replicationBytesOutPerSec = mbeanService.replicationBytesOutPerSec(broker);
                        if (replicationBytesOutPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(replicationBytesOutPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_PRODUCE_MESSAGE_CONVERSIONS:
                        MBeanVo produceMessageConv = mbeanService.produceMessageConversionsPerSec(broker);
                        if (produceMessageConv != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + Common.numberic(produceMessageConv.getOneMinute()));
                        }
                        break;
                    case KAFKA_OS_TOTAL_MEMORY:
                        long totalMemory = mbeanService.getOsTotalMemory(broker);
                        sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + totalMemory);
                        break;
                    case KAFKA_OS_FREE_MEMORY:
                        long freeMemory = mbeanService.getOsFreeMemory(broker);
                        sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + freeMemory);
                        break;
                    case KAFKA_SYSTEM_CPU_LOAD:
                        double systemCpuLoad = Double.parseDouble(kafkaJmxService.getData(broker, JMX.OPERATING_SYSTEM, kpi.getName()));
                        sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + systemCpuLoad);
                        break;
                    case KAFKA_PROCESS_CPU_LOAD:
                        double processCpuLoad = Double.parseDouble(kafkaJmxService.getData(broker, JMX.OPERATING_SYSTEM, kpi.getName()));
                        sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + processCpuLoad);
                        break;
                    case KAFKA_THREAD_COUNT:
                        int threadCount = Integer.parseInt(kafkaJmxService.getData(broker, JMX.THREADING, kpi.getName()));
                        sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue()) + threadCount);
                        break;
                    default:
                        break;
                }
            }
            if (sysKpi.getValue() == null) {
                continue;
            }
            sysKpi.setHost(host.length() == 0 ? "unkowns" : host.substring(0, host.length() - 1));
            result.add(sysKpi);
        }

        Optional<SysKpi> firstOsFree = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_OS_FREE_MEMORY.getCode())).findFirst();
        Optional<SysKpi> firstOsTotal = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_OS_TOTAL_MEMORY.getCode())).findFirst();
        Optional<SysKpi> firstSystemCpuLoad = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_SYSTEM_CPU_LOAD.getCode())).findFirst();
        Optional<SysKpi> firstProcessCpuLoad = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_PROCESS_CPU_LOAD.getCode())).findFirst();
        Optional<SysKpi> firstThreadCount = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_THREAD_COUNT.getCode())).findFirst();

        if (firstOsFree.isPresent() && firstOsTotal.isPresent()) {
            Double osFree = firstOsFree.get().getValue();
            Double osTotal = firstOsTotal.get().getValue();
            SysKpi sysKpi = new SysKpi();
            sysKpi.setKpi(SysKpi.KAFKA_KPI.KAFKA_OS_USED_MEMORY_PERCENTAGE.getCode());
            sysKpi.setCreateTime(now);
            sysKpi.setValue(Common.numberic((1 - osFree / osTotal) * 100));
            sysKpi.setHost(firstOsFree.get().getHost());
            result.add(sysKpi);
        }

        if (firstSystemCpuLoad.isPresent()) {
            SysKpi sysKpi = firstSystemCpuLoad.get();
            sysKpi.setValue((sysKpi.getValue() / brokers.size()) * 100);
        }
        if (firstProcessCpuLoad.isPresent()) {
            SysKpi sysKpi = firstProcessCpuLoad.get();
            sysKpi.setValue((sysKpi.getValue() / brokers.size()) * 100);
        }
        if (firstThreadCount.isPresent()) {
            SysKpi sysKpi = firstThreadCount.get();
            sysKpi.setValue((double) (sysKpi.getValue().intValue() / brokers.size()));
        }
        return result;
    }

    private KafkaBrokerVo getBrokerInfo(String brokerName) throws Exception {
        String brokerInfoJson = kafkaZkService.getData(String.format("%s/%s", Constants.ZK_BROKER_IDS_PATH, brokerName));
        JSONObject jsonObject = new JSONObject(brokerInfoJson);
        KafkaBrokerVo brokerVo = new KafkaBrokerVo();
        brokerVo.setHost(jsonObject.get("host").toString());
        brokerVo.setPort(jsonObject.get("port").toString());
        brokerVo.setEndpoints(jsonObject.get("endpoints").toString());
        brokerVo.setJmxPort(jsonObject.get("jmx_port").toString());
        brokerVo.setCreateTime(Common.format(new Date(jsonObject.getLong("timestamp"))));
        return brokerVo;
    }

    public List<String> listBrokerNames() throws Exception {
        return kafkaZkService.getChildren(Constants.ZK_BROKER_IDS_PATH);
    }

    private String getKafkaVersion(KafkaBrokerVo brokerInfo) {
        String result = " - ";
        try {
            result = kafkaJmxService.getData(brokerInfo, String.format("kafka.server:type=app-info,id=%s", brokerInfo.getName()), "Version");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getKafkaBrokerServer() throws Exception {
        StringBuilder kafkaUrls = new StringBuilder();
        List<KafkaBrokerVo> brokerInfoList = this.listBrokerInfos();
        if (brokerInfoList == null || brokerInfoList.size() < 1) {
            throw new BusinessException(ResultCode.KAFKA_NOT_RUNNING);
        }
        for (KafkaBrokerVo brokerInfo : brokerInfoList) {
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

    private void kafkaProducerDo(KafkaProducerAction kafkaProducerAction) throws Exception {
        KafkaProducer<String, String> kafkaProducer = null;
        try {
            Properties props = new Properties();
            props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer());
            props.put("acks", "all");
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, Constants.KAFKA_COMPRESS_TYPE);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            kafkaProducer = new KafkaProducer<>(props);
            kafkaProducerAction.action(kafkaProducer);
        } finally {
            if (kafkaProducer != null) {
                kafkaProducer.close();
            }
        }
    }

    private void kafkaConsumerDo(KafkaConsumerAction kafkaConsumerAction) throws Exception {
        KafkaConsumer kafkaConsumer = null;
        try {
            Properties props = new Properties();
            props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MONITOR);
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, Constants.KAFKA_COMPRESS_TYPE);
            props.setProperty("enable.auto.commit", "true");
            props.setProperty("auto.commit.interval.ms", "1000");
            props.setProperty("isolation.level", "read_committed");
            props.setProperty("auto.offset.reset", "earliest");
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

    private interface KafkaProducerAction {
        void action(KafkaProducer kafkaProducer);
    }

    private interface KafkaConsumerAction {
        void action(KafkaConsumer kafkaConsumer) throws Exception;
    }

}