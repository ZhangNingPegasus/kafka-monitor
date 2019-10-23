package com.pegasus.kafka.service.kafka;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.po.Out;
import com.pegasus.kafka.entity.vo.*;
import com.pegasus.kafka.service.core.KafkaService;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class KafkaTopicService {
    private final KafkaService kafkaService;

    public KafkaTopicService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public List<KafkaTopicInfo> listTopicNames(String searchTopicName, SearchType searchType) throws Exception {
        List<KafkaTopicInfo> topicInfoList = new ArrayList<>();
        List<String> topicNameList = kafkaService.listTopicNames();
        for (String topicName : topicNameList) {
            if (!StringUtils.isEmpty(searchTopicName)) {
                boolean isContinue = false;
                switch (searchType) {
                    case EQUALS:
                        isContinue = !topicName.equals(searchTopicName);
                        break;
                    case LIKE:
                        isContinue = !topicName.contains(searchTopicName);
                        break;
                }
                if (isContinue) {
                    continue;
                }
            }
            Out out = new Out();
            Stat stat = kafkaService.getTopicStat(topicName);
            List<String> partitionList = kafkaService.listPartitionIds(topicName);
            KafkaTopicInfo topicInfo = new KafkaTopicInfo();
            topicInfo.setTopicName(topicName);
            topicInfo.setPartitionNum(partitionList.size());
            topicInfo.setPartitionIndex(partitionList.toString());
            topicInfo.setCreateTime(Common.format(new Date(stat.getCtime())));
            topicInfo.setModifyTime(Common.format(new Date(stat.getMtime())));
            try {
                topicInfo.setLogSize(kafkaService.getLogSize(topicName, out));
            } catch (Exception e) {
                topicInfo.setLogSize(-1L);
            }
            topicInfo.setError(out.getError());
            topicInfoList.add(topicInfo);
        }
        return topicInfoList;
    }

    public List<KafkaTopicInfo> listTopicNames() throws Exception {
        return listTopicNames(null, null);
    }

    public List<KafkaTopicPartitionInfo> listTopicDetails(String topicName) throws Exception {
        List<KafkaTopicPartitionInfo> result = kafkaService.listTopicDetails(topicName);
        for (KafkaTopicPartitionInfo kafkaTopicPartitionInfo : result) {
            if (kafkaTopicPartitionInfo.getLeader() == null) {
                kafkaTopicPartitionInfo.setStrLeader(String.format(Constants.HOST_NOT_AVAIABLE));
                kafkaTopicPartitionInfo.setStrReplicas(String.format(Constants.HOST_NOT_AVAIABLE));
                kafkaTopicPartitionInfo.setStrIsr(String.format(Constants.HOST_NOT_AVAIABLE));

            } else {
                kafkaTopicPartitionInfo.setStrLeader(String.format("[%s] : (%s:%s)", kafkaTopicPartitionInfo.getLeader().getPartitionId(), kafkaTopicPartitionInfo.getLeader().getHost(), kafkaTopicPartitionInfo.getLeader().getPort()));

                StringBuilder strReplicas = new StringBuilder();
                for (KafkaTopicPartitionInfo.PartionInfo replica : kafkaTopicPartitionInfo.getReplicas()) {
                    strReplicas.append(String.format("[%s] : (%s:%s), ", replica.getPartitionId(), replica.getHost(), replica.getPort()));
                }
                kafkaTopicPartitionInfo.setStrReplicas(strReplicas.substring(0, strReplicas.length() - 2));

                StringBuilder strIsr = new StringBuilder();
                for (KafkaTopicPartitionInfo.PartionInfo isr : kafkaTopicPartitionInfo.getIsr()) {
                    strIsr.append(String.format("[%s] : (%s:%s), ", isr.getPartitionId(), isr.getHost(), isr.getPort()));
                }
                kafkaTopicPartitionInfo.setStrIsr(strIsr.substring(0, strIsr.length() - 2));
            }
        }
        return result;
    }

    public void add(String topicName, Integer partitionNumber, Integer replicationNumber) {
        try {
            kafkaService.createTopics(topicName, partitionNumber, replicationNumber);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOPIC_ALREADY_EXISTS);
        }
    }

    public List<MBeanInfo> listTopicMBean(String topicName) throws Exception {
        return kafkaService.listTopicMBean(topicName);
    }

    public void edit(String topicName, Integer partitionNumber) throws Exception {
        List<KafkaTopicInfo> topicInfoList = listTopicNames(topicName, SearchType.EQUALS);
        if (topicInfoList != null && topicInfoList.size() > 0) {
            KafkaTopicInfo topicInfo = topicInfoList.get(0);
            if (partitionNumber > topicInfo.getPartitionNum()) {
                kafkaService.alterTopics(topicName, partitionNumber);
            } else {
                throw new BusinessException(String.format("新的分区数量必须大于%s", topicInfo.getPartitionNum()));
            }
        } else {
            throw new BusinessException(ResultCode.TOPIC_NOT_EXISTS);
        }
    }

    public void delete(String topicName) throws Exception {
        List<KafkaConsumerInfo> kafkaConsumerInfos = kafkaService.listKafkaConsumers();
        for (KafkaConsumerInfo kafkaConsumerInfo : kafkaConsumerInfos) {
            if (kafkaConsumerInfo.getActiveTopicNames().contains(topicName)) {
                throw new BusinessException(ResultCode.TOPIC_IS_RUNNING);
            }
        }
        kafkaService.deleteTopics(topicName);
    }

    public String listTopicSize(String topicName) throws Exception {
        return kafkaService.listTopicSize(topicName);
    }

    public void sendMessage(String topicName, String content) throws Exception {
        kafkaService.sendMessage(topicName, content);
    }

    public long getLogsize(String topicName) throws Exception {
        Long result = 0L;
        List<KafkaTopicPartitionInfo> topicDetails = listTopicDetails(topicName);
        for (KafkaTopicPartitionInfo topicDetail : topicDetails) {
            result += topicDetail.getLogsize();
        }
        return result;
    }

    public List<KafkaMessageInfo> listMessages(String topicName, Integer[] partitionNums, Integer pageNum, Integer pageSize) throws Exception {
        List<KafkaMessageInfo> result = new ArrayList<>(pageSize * partitionNums.length);

        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.KAFKA_MONITOR_SYSTEM_GROUP_NAME);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());

        KafkaConsumer<String, String> consumer = null;
        try {
            consumer = new KafkaConsumer<>(props);
            List<TopicPartition> topics = new ArrayList<>(partitionNums.length);
            for (int i = 0; i < partitionNums.length; i++) {
                TopicPartition tp = new TopicPartition(topicName, partitionNums[i]);
                topics.add(tp);
            }
            consumer.assign(topics);

            for (TopicPartition tp : topics) {
                Map<TopicPartition, Long> offsets = consumer.endOffsets(Collections.singleton(tp));
                long num = pageSize * pageNum;

                if (offsets.get(tp).longValue() < num) {
                    num = offsets.get(tp).longValue();
                }

                consumer.seek(tp, offsets.get(tp).longValue() - num);
            }

            boolean flag = true;
            while (flag) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    KafkaMessageInfo kafkaMessageInfo = new KafkaMessageInfo();
                    kafkaMessageInfo.setTopicName(record.topic());
                    kafkaMessageInfo.setPartitionId(String.valueOf(record.partition()));
                    kafkaMessageInfo.setOffset(String.valueOf(record.offset()));
                    kafkaMessageInfo.setKey(record.key());
                    kafkaMessageInfo.setTimestamp(record.timestamp());
                    kafkaMessageInfo.setCreateTime(Common.format(new Date(kafkaMessageInfo.getTimestamp())));
                    kafkaMessageInfo.setValue(record.value());
                    result.add(kafkaMessageInfo);
                }
                if (records.isEmpty()) {
                    flag = false;
                }
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
        result.sort((o1, o2) -> (int) (o2.getTimestamp() - o1.getTimestamp()));
        return result.stream().skip(pageSize * (pageNum - 1)).limit(pageSize).collect(Collectors.toList());
    }

    public enum SearchType {
        LIKE,
        EQUALS
    }
}