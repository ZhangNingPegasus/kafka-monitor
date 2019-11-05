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

    public List<KafkaTopicPartitionInfo> listTopicDetails(String topicName, boolean needLogsize) throws Exception {
        final List<KafkaTopicPartitionInfo> result = new ArrayList<>();
        AtomicReference<DescribeTopicsResult> describeTopicsResult = new AtomicReference<>();

        kafkaAdminClientDo(kafkaAdminClient -> describeTopicsResult.set(kafkaAdminClient.describeTopics(Collections.singletonList(topicName))));

        Map<String, TopicDescription> topicDescriptionMap = describeTopicsResult.get().all().get();

        for (Map.Entry<String, TopicDescription> pair : topicDescriptionMap.entrySet()) {
            TopicDescription topicDescription = pair.getValue();
            for (TopicPartitionInfo partition : topicDescription.partitions()) {
                KafkaTopicPartitionInfo topicPartitionVo = new KafkaTopicPartitionInfo();
                topicPartitionVo.setTopicName(pair.getKey());
                topicPartitionVo.setPartitionId(Integer.toString(partition.partition()));
                if (partition.leader() == null) {
                    topicPartitionVo.setLogsize(-1L);
                } else {
                    topicPartitionVo.setLeader(new KafkaTopicPartitionInfo.PartionInfo(
                            Integer.toString(partition.leader().id()),
                            partition.leader().host(),
                            Integer.toString(partition.leader().port()),
                            partition.leader().rack()
                    ));

                    List<KafkaTopicPartitionInfo.PartionInfo> partitionVoList = new ArrayList<>();
                    for (Node replica : partition.replicas()) {
                        partitionVoList.add(new KafkaTopicPartitionInfo.PartionInfo(
                                Integer.toString(replica.id()),
                                replica.host(),
                                Integer.toString(replica.port()),
                                replica.rack()
                        ));
                    }
                    topicPartitionVo.setReplicas(partitionVoList);

                    List<KafkaTopicPartitionInfo.PartionInfo> isrList = new ArrayList<>();
                    for (Node isr : partition.isr()) {
                        isrList.add(new KafkaTopicPartitionInfo.PartionInfo(
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
                for (KafkaTopicPartitionInfo topicPartitionVo : result) {
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
        Map<TopicPartition, Long> startLogSize = kafkaConsumer.beginningOffsets(Collections.singleton(tp));
        return endLogSize.get(tp) - startLogSize.get(tp);
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
                if (!groupId.startsWith(Constants.KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX)) {
                    DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(Collections.singletonList(groupId));

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

        if (result.size() < partitionOffset.size()) {
            for (OffsetInfo offsetInfo : result) {
                partitionOffset.remove(offsetInfo.getPartitionId());
            }

            for (Map.Entry<Integer, Long> entry : partitionOffset.entrySet()) {
                OffsetInfo offsetInfo = new OffsetInfo();
                offsetInfo.setPartitionId(entry.getKey());
                offsetInfo.setOffset(entry.getValue());
                offsetInfo.setLogSize(-1L);
                offsetInfo.setLag(-1L);
                offsetInfo.setConsumerId(String.format(Constants.PARTITION_NOT_AVAIABLE, entry.getKey()));
                result.add(offsetInfo);
            }
        }
        return result;
    }

    private String getConsumerId(List<KafkaConsumerInfo> kafkaConsumerInfoList, String topicName, Integer partitionId) {
        if (kafkaConsumerInfoList == null) {
            return null;
        }
        for (KafkaConsumerInfo kafkaConsumerInfo : kafkaConsumerInfoList) {
            for (KafkaConsumerInfo.Meta meta : kafkaConsumerInfo.getMetaList()) {
                for (KafkaConsumerInfo.TopicSubscriber topicSubscriber : meta.getTopicSubscriberList()) {
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
        List<KafkaTopicPartitionInfo> topicDetails = this.listTopicDetails(topicName, false);

        kafkaConsumerDo(kafkaConsumer -> {
            Set<TopicPartition> tps = new HashSet<>();
            for (String partitionId : partitionIds) {
                Optional<KafkaTopicPartitionInfo> first = topicDetails.stream().filter(p -> p.getPartitionId().equals(partitionId)).findFirst();
                if (!first.isPresent()) {
                    continue;
                }
                KafkaTopicPartitionInfo kafkaTopicPartitionInfo = first.get();
                if (kafkaTopicPartitionInfo.getLeader() == null) {
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
        List<KafkaTopicPartitionInfo> topicDetails = listTopicDetails(topicName, false);
        List<KafkaBrokerInfo> brokerVoList = listBrokerInfos();

        for (KafkaTopicPartitionInfo topicDetail : topicDetails) {
            if (topicDetail.getLeader() == null) {
                continue;
            }
            KafkaTopicPartitionInfo.PartionInfo leader = topicDetail.getLeader();
            Optional<KafkaBrokerInfo> first = brokerVoList.stream().filter(p -> p.getHost().equals(leader.getHost())).findFirst();
            if (first.isPresent()) {
                KafkaBrokerInfo brokerVo = first.get();
                String size = kafkaJmxService.getData(brokerVo, String.format("kafka.log:type=Log,name=Size,topic=%s,partition=%s", topicName, topicDetail.getPartitionId()), "Value");
                totalSize += Long.parseLong(size);
            }
        }
        return Common.convertSize(totalSize);
    }

    public List<KafkaBrokerInfo> listBrokerInfos() throws Exception {
        List<String> brokerList = this.listBrokerNames();
        List<KafkaBrokerInfo> result = new ArrayList<>(brokerList.size());
        for (String brokerName : brokerList) {
            KafkaBrokerInfo brokerInfo = this.getBrokerInfo(brokerName);
            brokerInfo.setName(brokerName);
            brokerInfo.setVersion(getKafkaVersion(brokerInfo));
            result.add(brokerInfo);
        }
        return result;
    }

    public List<MBeanInfo> listTopicMBean(String topicName) throws Exception {
        Map<String, MBeanInfo> result = new LinkedHashMap<>();
        List<KafkaBrokerInfo> brokerInfoList = this.listBrokerInfos();
        for (KafkaBrokerInfo brokerInfo : brokerInfoList) {
            MBeanInfo bytesIn = mBeanService.bytesInPerSec(brokerInfo, topicName);
            MBeanInfo bytesOut = mBeanService.bytesOutPerSec(brokerInfo, topicName);
            MBeanInfo bytesRejected = mBeanService.bytesRejectedPerSec(brokerInfo, topicName);
            MBeanInfo failedFetchRequest = mBeanService.failedFetchRequestsPerSec(brokerInfo, topicName);
            MBeanInfo failedProduceRequest = mBeanService.failedProduceRequestsPerSec(brokerInfo, topicName);
            MBeanInfo messageIn = mBeanService.messagesInPerSec(brokerInfo, topicName);
            MBeanInfo produceMessageConversions = mBeanService.produceMessageConversionsPerSec(brokerInfo, topicName);
            MBeanInfo totalFetchRequests = mBeanService.totalFetchRequestsPerSec(brokerInfo, topicName);
            MBeanInfo totalProduceRequests = mBeanService.totalProduceRequestsPerSec(brokerInfo, topicName);

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

        for (Map.Entry<String, MBeanInfo> entry : result.entrySet()) {
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

    private void assembleMBeanInfo(Map<String, MBeanInfo> mbeans, String mBeanInfoKey, MBeanInfo mBeanInfo) {
        if (mbeans.containsKey(mBeanInfoKey) && mBeanInfo != null) {
            DecimalFormat formatter = new DecimalFormat("###.##");
            MBeanInfo mbeanInfo = mbeans.get(mBeanInfoKey);
            String fifteenMinuteOld = mbeanInfo.getFifteenMinute() == null ? "0.0" : mbeanInfo.getFifteenMinute();
            String fifteenMinuteLastest = mBeanInfo.getFifteenMinute() == null ? "0.0" : mBeanInfo.getFifteenMinute();
            String fiveMinuteOld = mbeanInfo.getFiveMinute() == null ? "0.0" : mbeanInfo.getFiveMinute();
            String fiveMinuteLastest = mBeanInfo.getFiveMinute() == null ? "0.0" : mBeanInfo.getFiveMinute();
            String meanRateOld = mbeanInfo.getMeanRate() == null ? "0.0" : mbeanInfo.getMeanRate();
            String meanRateLastest = mBeanInfo.getMeanRate() == null ? "0.0" : mBeanInfo.getMeanRate();
            String oneMinuteOld = mbeanInfo.getOneMinute() == null ? "0.0" : mbeanInfo.getOneMinute();
            String oneMinuteLastest = mBeanInfo.getOneMinute() == null ? "0.0" : mBeanInfo.getOneMinute();
            double fifteenMinute = Common.numberic(fifteenMinuteOld) + Common.numberic(fifteenMinuteLastest);
            double fiveMinute = Common.numberic(fiveMinuteOld) + Common.numberic(fiveMinuteLastest);
            double meanRate = Common.numberic(meanRateOld) + Common.numberic(meanRateLastest);
            double oneMinute = Common.numberic(oneMinuteOld) + Common.numberic(oneMinuteLastest);
            mbeanInfo.setFifteenMinute(formatter.format(fifteenMinute));
            mbeanInfo.setFiveMinute(formatter.format(fiveMinute));
            mbeanInfo.setMeanRate(formatter.format(meanRate));
            mbeanInfo.setOneMinute(formatter.format(oneMinute));
            mbeanInfo.setName(mBeanInfoKey);
        } else {
            mbeans.put(mBeanInfoKey, mBeanInfo);
        }
    }

    public Long getLogSize(String topicName, Out out) throws Exception {
        Long result = 0L;
        List<String> listPartitionNames = listPartitionIds(topicName);
        List<KafkaTopicPartitionInfo> topicDetails = listTopicDetails(topicName, false);
        for (String listPartitionName : listPartitionNames) {
            Optional<KafkaTopicPartitionInfo> first = topicDetails.stream().filter(p -> listPartitionName.equals(p.getPartitionId())).findFirst();
            if (!first.isPresent()) {
                continue;
            }
            KafkaTopicPartitionInfo kafkaTopicPartitionInfo = first.get();
            if (kafkaTopicPartitionInfo.getLeader() == null) {
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
        return kafkaZkService.getChildren(Constants.ZK_BROKERS_TOPICS_PATH).stream().filter(p -> !Constants.KAFKA_CONSUMER_OFFSETS.equals(p)).collect(Collectors.toList());
    }

    public Stat getTopicStat(String topicName) throws Exception {
        Stat stat = new Stat();
        kafkaZkService.getData(String.format("%s/%s", Constants.ZK_BROKERS_TOPICS_PATH, topicName), stat);
        return stat;
    }

    public List<SysKpi> kpi(Date now) throws Exception {
        List<SysKpi> result = new ArrayList<>(SysKpi.KAFKA_KPI.values().length);
        List<KafkaBrokerInfo> brokers = this.listBrokerInfos();

        for (SysKpi.KAFKA_KPI kpi : SysKpi.KAFKA_KPI.values()) {
            if (StringUtils.isEmpty(kpi.getName())) {
                continue;
            }
            SysKpi sysKpi = new SysKpi();
            sysKpi.setKpi(kpi.getCode());
            sysKpi.setCreateTime(now);
            StringBuilder host = new StringBuilder();
            for (KafkaBrokerInfo broker : brokers) {
                host.append(broker.getHost()).append(",");
                switch (kpi) {
                    case KAFKA_MESSAGES_IN:
                        MBeanInfo msg = mbeanService.messagesInPerSec(broker);
                        if (msg != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(msg.getOneMinute())));
                        }
                        break;
                    case KAFKA_BYTES_IN:
                        MBeanInfo bin = mbeanService.bytesInPerSec(broker);
                        if (bin != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(bin.getOneMinute())));
                        }
                        break;
                    case KAFKA_BYTES_OUT:
                        MBeanInfo bout = mbeanService.bytesOutPerSec(broker);
                        if (bout != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(bout.getOneMinute())));
                        }
                        break;
                    case KAFKA_BYTES_REJECTED:
                        MBeanInfo bytesRejectedPerSec = mbeanService.bytesRejectedPerSec(broker);
                        if (bytesRejectedPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(bytesRejectedPerSec.getOneMinute())));
                        }
                        break;
                    case KAFKA_FAILED_FETCH_REQUEST:
                        MBeanInfo failedFetchRequestsPerSec = mbeanService.failedFetchRequestsPerSec(broker);
                        if (failedFetchRequestsPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(failedFetchRequestsPerSec.getOneMinute())));
                        }
                        break;
                    case KAFKA_FAILED_PRODUCE_REQUEST:
                        MBeanInfo failedProduceRequestsPerSec = mbeanService.failedProduceRequestsPerSec(broker);
                        if (failedProduceRequestsPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(failedProduceRequestsPerSec.getOneMinute())));
                        }
                        break;
                    case KAFKA_TOTAL_FETCH_REQUESTS_PER_SEC:
                        MBeanInfo totalFetchRequests = mbeanService.totalFetchRequestsPerSec(broker);
                        if (totalFetchRequests != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(totalFetchRequests.getOneMinute())));
                        }
                        break;
                    case KAFKA_TOTAL_PRODUCE_REQUESTS_PER_SEC:
                        MBeanInfo totalProduceRequestsPerSec = mbeanService.totalProduceRequestsPerSec(broker);
                        if (totalProduceRequestsPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(totalProduceRequestsPerSec.getOneMinute())));
                        }
                        break;
                    case KAFKA_REPLICATION_BYTES_IN_PER_SEC:
                        MBeanInfo replicationBytesInPerSec = mbeanService.replicationBytesInPerSec(broker);
                        if (replicationBytesInPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(replicationBytesInPerSec.getOneMinute())));
                        }
                        break;
                    case KAFKA_REPLICATION_BYTES_OUT_PER_SEC:
                        MBeanInfo replicationBytesOutPerSec = mbeanService.replicationBytesOutPerSec(broker);
                        if (replicationBytesOutPerSec != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(replicationBytesOutPerSec.getOneMinute())));
                        }
                        break;
                    case KAFKA_PRODUCE_MESSAGE_CONVERSIONS:
                        MBeanInfo produceMessageConv = mbeanService.produceMessageConversionsPerSec(broker);
                        if (produceMessageConv != null) {
                            sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + Common.numberic(produceMessageConv.getOneMinute())));
                        }
                        break;
                    case KAFKA_OS_TOTAL_MEMORY:
                        long totalMemory = mbeanService.getOsTotalMemory(broker);
                        sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + totalMemory));
                        break;
                    case KAFKA_OS_FREE_MEMORY:
                        long freeMemory = mbeanService.getOsFreeMemory(broker);
                        sysKpi.setValue(Common.numberic(sysKpi.getValue() == null ? 0D : sysKpi.getValue() + freeMemory));
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

        if (firstOsFree.isPresent() && firstOsTotal.isPresent()) {
            Double osFree = firstOsFree.get().getValue();
            Double osTotal = firstOsTotal.get().getValue();
            SysKpi sysKpi = new SysKpi();
            sysKpi.setKpi(SysKpi.KAFKA_KPI.KAFKA_OS_USED_MEMORY_PERCENTAGE.getCode());
            sysKpi.setCreateTime(now);
            sysKpi.setValue(Common.numberic((osFree / osTotal) * 100));
            sysKpi.setHost(firstOsFree.get().getHost());
            result.add(sysKpi);
        }

        return result;
    }

    private KafkaBrokerInfo getBrokerInfo(String brokerName) throws Exception {
        String brokerInfoJson = kafkaZkService.getData(String.format("%s/%s", Constants.ZK_BROKER_IDS_PATH, brokerName));
        JSONObject jsonObject = new JSONObject(brokerInfoJson);
        KafkaBrokerInfo brokerVo = new KafkaBrokerInfo();
        brokerVo.setHost(jsonObject.get("host").toString());
        brokerVo.setPort(jsonObject.get("port").toString());
        brokerVo.setEndpoints(jsonObject.get("endpoints").toString());
        brokerVo.setJmxPort(jsonObject.get("jmx_port").toString());
        brokerVo.setCreateTime(Common.format(new Date(jsonObject.getLong("timestamp"))));
        return brokerVo;
    }

    private List<String> listBrokerNames() throws Exception {
        return kafkaZkService.getChildren(Constants.ZK_BROKER_IDS_PATH);
    }

    private String getKafkaVersion(KafkaBrokerInfo brokerInfo) {
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
        List<KafkaBrokerInfo> brokerInfoList = this.listBrokerInfos();
        if (brokerInfoList == null || brokerInfoList.size() < 1) {
            throw new BusinessException(ResultCode.KAFKA_NOT_RUNNING);
        }
        for (KafkaBrokerInfo brokerInfo : brokerInfoList) {
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
