package com.pegasus.kafka.service.kafka;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.entity.po.Out;
import com.pegasus.kafka.entity.vo.*;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.dto.SysLagService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import com.pegasus.kafka.service.dto.TopicRecordService;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class KafkaTopicService {
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;
    private final SysLagService sysLagService;
    private final SysLogSizeService sysLogSizeService;
    private final KafkaConsumerService kafkaConsumerService;

    public KafkaTopicService(KafkaService kafkaService, TopicRecordService topicRecordService, SysLagService sysLagService, @Lazy SysLogSizeService sysLogSizeService, KafkaConsumerService kafkaConsumerService) {
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
        this.sysLagService = sysLagService;
        this.sysLogSizeService = sysLogSizeService;
        this.kafkaConsumerService = kafkaConsumerService;
    }

    @TranRead
    public List<KafkaTopicInfo> listTopics(String searchTopicName, SearchType searchType) throws Exception {
        List<KafkaTopicInfo> topicInfoList = new ArrayList<>();
        List<String> topicNameList = kafkaService.listTopicNames();
        List<KafkaConsumerInfo> kafkaConsumerInfoList = kafkaConsumerService.listKafkaConsumers();

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

            List<String> SubscribeGroupIdList = kafkaConsumerInfoList.stream().filter(p -> p.getTopicNames().contains(topicName)).map(p -> p.getGroupId()).distinct().collect(Collectors.toList());
            Out out = new Out();
            try {
                Stat stat = kafkaService.getTopicStat(topicName);
                List<String> partitionList = kafkaService.listPartitionIds(topicName);
                KafkaTopicInfo topicInfo = new KafkaTopicInfo();
                topicInfo.setTopicName(topicName);
                topicInfo.setPartitionNum(partitionList.size());
                topicInfo.setPartitionIndex(partitionList.toString());
                topicInfo.setCreateTimeLong(stat.getCtime());
                topicInfo.setModifyTimeLong(stat.getMtime());
                topicInfo.setSubscribeNums(SubscribeGroupIdList.size());
                topicInfo.setSubscribeGroupIds(SubscribeGroupIdList.toArray(new String[]{}));
                topicInfo.setCreateTime(Common.format(new Date(stat.getCtime())));
                topicInfo.setModifyTime(Common.format(new Date(stat.getMtime())));
                try {
                    topicInfo.setLogSize(kafkaService.getLogSize(topicName, out));


                    Long day1 = sysLogSizeService.getHistoryLogSize(1);
                    Long day2 = sysLogSizeService.getHistoryLogSize(2);
                    Long day3 = sysLogSizeService.getHistoryLogSize(3);

                    topicInfo.setTodayLogSize(topicInfo.getLogSize() - day1);
                    topicInfo.setYesterdayLogSize(day1 - day2);
                    topicInfo.setTdbyLogSize(day2 - day3);

                } catch (Exception e) {
                    topicInfo.setLogSize(-1L);
                }


                topicInfo.setError(out.getError());
                topicInfoList.add(topicInfo);
            } catch (Exception ignored) {
            }
        }

        topicInfoList.sort((o1, o2) -> (int) (o2.getCreateTimeLong() - o1.getCreateTimeLong()));
        return topicInfoList;
    }

    public List<KafkaTopicInfo> listTopics() throws Exception {
        return listTopics(null, null);
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
        List<KafkaTopicInfo> topicInfoList = listTopics(topicName, SearchType.EQUALS);
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

    @TranSave
    public void delete(String topicName) throws Exception {
        List<KafkaConsumerInfo> kafkaConsumerInfos = kafkaService.listKafkaConsumers();
        for (KafkaConsumerInfo kafkaConsumerInfo : kafkaConsumerInfos) {
            if (kafkaConsumerInfo.getActiveTopicNames().contains(topicName)) {
                throw new BusinessException(ResultCode.TOPIC_IS_RUNNING);
            }
        }
        kafkaService.deleteTopic(topicName);
        try {
            topicRecordService.dropTable(topicName);
        } catch (Exception ignored) {
        }
        sysLagService.deleteTopic(topicName);
        sysLogSizeService.deleteTopic(topicName);
        Thread.sleep(500);
    }

    public String listTopicSize(String topicName) throws Exception {
        return kafkaService.listTopicSize(topicName);
    }

    public void sendMessage(String topicName, String key, String content) throws Exception {
        kafkaService.sendMessage(topicName, key, content);
    }

    public void sendMessage(String topicName, String content) throws Exception {
        kafkaService.sendMessage(topicName, content);
    }

    public long getLogsize(String topicName, String partitionId) throws Exception {
        Long result = 0L;
        List<KafkaTopicPartitionInfo> topicDetails = listTopicDetails(topicName);
        for (KafkaTopicPartitionInfo topicDetail : topicDetails) {
            if (StringUtils.isEmpty(partitionId)) {
                result += topicDetail.getLogsize();
            } else if (partitionId.equals(topicDetail.getPartitionId())) {
                result += topicDetail.getLogsize();
            }
        }
        return result;
    }

    public long getLogsize(String topicName) throws Exception {
        return getLogsize(topicName, null);
    }


    public List<KafkaTopicRecordInfo> listMessages(IPage page, String topicName, Integer partitionNum, String key, Date from, Date to) {
        List<TopicRecord> topicRecordList = topicRecordService.listMessages(page, topicName, partitionNum, key, from, to);
        List<KafkaTopicRecordInfo> result = new ArrayList<>(topicRecordList.size());

        for (TopicRecord topicRecord : topicRecordList) {
            result.add(topicRecord.toVo());
        }

        return result;
    }

    public List<KafkaTopicRecordInfo> listMessages(String topicName, Integer partitionNum, Integer pageNum, Integer pageSize) throws Exception {
        List<Integer> partitionNumList = new ArrayList<>();
        if (partitionNum < 0) {
            List<KafkaTopicPartitionInfo> topicDetails = this.listTopicDetails(topicName);
            partitionNumList.addAll(topicDetails.stream().map(p -> Integer.parseInt(p.getPartitionId())).collect(Collectors.toList()));
        } else {
            partitionNumList.add(partitionNum);
        }

        List<KafkaTopicRecordInfo> result = new ArrayList<>(pageSize * partitionNumList.size());

        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MONITOR);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());

        KafkaConsumer<String, String> consumer = null;
        try {
            consumer = new KafkaConsumer<>(props);
            List<TopicPartition> topics = new ArrayList<>(partitionNumList.size());
            for (Integer partitionId : partitionNumList) {
                TopicPartition tp = new TopicPartition(topicName, partitionId);
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
                    KafkaTopicRecordInfo kafkaMessageInfo = new KafkaTopicRecordInfo();
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