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
import com.pegasus.kafka.service.record.CoreService;
import org.apache.commons.lang3.RandomUtils;
import org.apache.kafka.clients.admin.AlterPartitionReassignmentsResult;
import org.apache.kafka.clients.admin.NewPartitionReassignment;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The service for Kafka's topic.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class KafkaTopicService {
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;
    private final SysLagService sysLagService;
    private final SysLogSizeService sysLogSizeService;
    private final KafkaConsumerService kafkaConsumerService;
    private final CoreService kafkaRecordService;

    public KafkaTopicService(KafkaService kafkaService, TopicRecordService topicRecordService, SysLagService sysLagService, @Lazy SysLogSizeService sysLogSizeService, KafkaConsumerService kafkaConsumerService, CoreService kafkaRecordService) {
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
        this.sysLagService = sysLagService;
        this.sysLogSizeService = sysLogSizeService;
        this.kafkaConsumerService = kafkaConsumerService;
        this.kafkaRecordService = kafkaRecordService;
    }

    @TranRead
    public List<KafkaTopicVo> listTopicVos(List<String> topicNameList) throws Exception {
        List<KafkaTopicVo> topicInfoList = new ArrayList<>(topicNameList.size());

        List<KafkaConsumerVo> kafkaConsumerVoList = kafkaConsumerService.listKafkaConsumers();

        for (String topicName : topicNameList) {
            KafkaTopicVo topicInfo = new KafkaTopicVo(topicName);



            List<String> subscribeGroupIdList = kafkaConsumerVoList.stream().filter(p -> p.getTopicNames().contains(topicName)).map(KafkaConsumerVo::getGroupId).distinct().collect(Collectors.toList());
            topicInfo.setSubscribeNums(subscribeGroupIdList.size());
            topicInfo.setSubscribeGroupIds(subscribeGroupIdList.toArray(new String[]{}));

            List<String> partitionList = kafkaService.listPartitionIds(topicName);
            topicInfo.setPartitionNum(partitionList.size());
            topicInfo.setPartitionIndex(partitionList.toString());

            Stat stat = kafkaService.getTopicStat(topicName);
            topicInfo.setCreateTimeLong(stat.getCtime());
            topicInfo.setModifyTimeLong(stat.getMtime());
            topicInfo.setCreateTime(Common.format(new Date(stat.getCtime())));
            topicInfo.setModifyTime(Common.format(new Date(stat.getMtime())));

            topicInfoList.add(topicInfo);
        }

        return topicInfoList;
    }

    @TranRead
    public KafkaTopicVo listTopicLogSize(String topicName) {
        KafkaTopicVo result = new KafkaTopicVo(topicName);
        result.setLogSize(topicRecordService.listMaxOffsetCount(topicName));
        try {
            result.setLogSize(topicRecordService.listMaxOffsetCount(topicName));
            result.setDay0LogSize(sysLogSizeService.getHistoryLogSize(topicName, 0)); //今天
            result.setDay1LogSize(sysLogSizeService.getHistoryLogSize(topicName, 1)); //昨天
            result.setDay2LogSize(sysLogSizeService.getHistoryLogSize(topicName, 2)); //前天
            result.setDay3LogSize(sysLogSizeService.getHistoryLogSize(topicName, 3)); //前3天
            result.setDay4LogSize(sysLogSizeService.getHistoryLogSize(topicName, 4)); //前4天
            result.setDay5LogSize(sysLogSizeService.getHistoryLogSize(topicName, 5)); //前5天
            result.setDay6LogSize(sysLogSizeService.getHistoryLogSize(topicName, 6)); //前6天

        } catch (Exception ignored) {
            Out out = new Out();
            try {
                result.setLogSize(kafkaService.getLogSize(topicName, out));
            } catch (Exception e) {
                result.setLogSize(-1L);
            }
            result.setError(out.getError());
            result.setDay0LogSize(result.getLogSize());
            result.setDay1LogSize(0L);
            result.setDay2LogSize(0L);
            result.setDay3LogSize(0L);
            result.setDay4LogSize(0L);
            result.setDay5LogSize(0L);
            result.setDay6LogSize(0L);
        }
        return result;
    }

    public List<KafkaTopicPartitionVo> listTopicDetails(String topicName) throws Exception {
        List<KafkaTopicPartitionVo> result = kafkaService.listTopicDetails(topicName, true);
        for (KafkaTopicPartitionVo kafkaTopicPartitionVo : result) {
            if (kafkaTopicPartitionVo.getLeader() == null) {
                kafkaTopicPartitionVo.setStrLeader(Constants.HOST_NOT_AVAIABLE);
                kafkaTopicPartitionVo.setStrReplicas(Constants.HOST_NOT_AVAIABLE);
                kafkaTopicPartitionVo.setStrIsr(Constants.HOST_NOT_AVAIABLE);
            } else {
                kafkaTopicPartitionVo.setStrLeader(String.format("[%s] : (%s:%s)", kafkaTopicPartitionVo.getLeader().getPartitionId(), kafkaTopicPartitionVo.getLeader().getHost(), kafkaTopicPartitionVo.getLeader().getPort()));

                StringBuilder strReplicas = new StringBuilder();
                for (KafkaTopicPartitionVo.PartionInfo replica : kafkaTopicPartitionVo.getReplicas()) {
                    strReplicas.append(String.format("[%s] : (%s:%s), ", replica.getPartitionId(), replica.getHost(), replica.getPort()));
                }
                kafkaTopicPartitionVo.setStrReplicas(strReplicas.substring(0, strReplicas.length() - 2));

                StringBuilder strIsr = new StringBuilder();
                for (KafkaTopicPartitionVo.PartionInfo isr : kafkaTopicPartitionVo.getIsr()) {
                    strIsr.append(String.format("[%s] : (%s:%s), ", isr.getPartitionId(), isr.getHost(), isr.getPort()));
                }
                kafkaTopicPartitionVo.setStrIsr(strIsr.substring(0, strIsr.length() - 2));
            }
        }
        return result;
    }

    public void add(String topicName, Integer partitionNumber, Integer replicationNumber) throws Exception {
        try {
            kafkaService.createTopics(topicName, partitionNumber, replicationNumber);
        } catch (Exception e) {
            if (e.getMessage().contains("already exists.")) {
                throw new BusinessException(ResultCode.TOPIC_ALREADY_EXISTS);
            }
            throw e;
        }
    }

    public List<MBeanVo> listTopicMBean(String topicName) throws Exception {
        return kafkaService.listTopicMBean(topicName);
    }

    public void edit(String topicName, Integer partitionCount, Integer replicationCount) throws Exception {
        List<String> partitionIds = kafkaService.listPartitionIds(topicName);
        if (null != partitionCount && partitionCount != partitionIds.size()) {
            if (partitionCount < 1) {
                return;
            } else if (partitionCount > partitionIds.size()) {
                kafkaService.alterTopics(topicName, partitionCount);
                kafkaRecordService.uninstallTopicName(topicName);
            } else {
                throw new BusinessException(String.format("新的分区数量必须大于%s", partitionIds.size()));
            }
        }

        if (null != partitionCount) {
            if (replicationCount < 1) {
                return;
            }
            List<KafkaBrokerVo> kafkaBrokerVos = kafkaService.listBrokerInfos();

            if (replicationCount > kafkaBrokerVos.size()) {
                throw new BusinessException(String.format("副本分片数量不能大于kafka节点数[%s]", kafkaBrokerVos.size()));
            }

            List<KafkaTopicPartitionVo> topicDetails = this.listTopicDetails(topicName);
            if (null == topicDetails) {
                return;
            } else if (topicDetails.size() > 0 && topicDetails.get(0).getReplicas().size() == replicationCount) {
                return;
            }

            Map<TopicPartition, Optional<NewPartitionReassignment>> map = new HashMap<>();

            for (KafkaTopicPartitionVo topicPartitionVo : topicDetails) {
                List<Integer> newPartitions = new ArrayList<>();
                if (replicationCount < kafkaBrokerVos.size()) {
                    for (int i = 0; i < replicationCount; i++) {
                        int nameInt = Integer.parseInt(kafkaBrokerVos.get(RandomUtils.nextInt(0, replicationCount + 1)).getName());
                        while (newPartitions.contains(nameInt)) {
                            nameInt = Integer.parseInt(kafkaBrokerVos.get(RandomUtils.nextInt(0, replicationCount + 1)).getName());
                        }
                        newPartitions.add(nameInt);
                    }
                } else {
                    while (newPartitions.size() < replicationCount) {
                        for (KafkaBrokerVo kafkaBrokerVo : kafkaBrokerVos) {
                            newPartitions.add(Integer.parseInt(kafkaBrokerVo.getName()));
                        }
                    }
                }
                Collections.shuffle(newPartitions);
                map.put(new TopicPartition(topicName, Integer.parseInt(topicPartitionVo.getPartitionId())), Optional.of(new NewPartitionReassignment(newPartitions)));
            }

            kafkaService.kafkaAdminClientDo(kafkaAdminClient -> {
                AlterPartitionReassignmentsResult alterPartitionReassignmentsResult = kafkaAdminClient.alterPartitionReassignments(map);
                alterPartitionReassignmentsResult.all().get();
            });
        }
    }

    @TranSave
    public void delete(String topicName) throws Exception {
        List<KafkaConsumerVo> kafkaConsumerVoList = kafkaService.listKafkaConsumers(null);
        for (KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
            if (kafkaConsumerVo.getMetaList().size() == 1) {
                if (kafkaConsumerVo.getMetaList().get(0).getConsumerGroupState() == ConsumerGroupState.EMPTY) {
                    break;
                }
            }
            if (kafkaConsumerVo.getActiveTopicNames().contains(topicName)) {
                throw new BusinessException(ResultCode.TOPIC_IS_RUNNING);
            }
        }

        kafkaRecordService.uninstallTopicName(topicName);
        sysLagService.deleteTopic(topicName);
        sysLogSizeService.deleteTopic(topicName);
        topicRecordService.truncateTable(topicName);
        Thread.sleep(1000);
        kafkaService.deleteTopic(topicName);
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
        List<KafkaTopicPartitionVo> topicDetails = listTopicDetails(topicName);
        for (KafkaTopicPartitionVo topicDetail : topicDetails) {
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

    public List<KafkaTopicRecordVo> listMessages(IPage page, String topicName, Integer partitionId, Long offset, String key, Date from, Date to) {
            List<TopicRecord> topicRecordList = topicRecordService.listRecords(page, topicName, partitionId, offset, key, from, to);
            List<KafkaTopicRecordVo> result = new ArrayList<>(topicRecordList.size());
            for (TopicRecord topicRecord : topicRecordList) {
                result.add(topicRecord.toVo());
            }
            return result;
    }

    public enum SearchType {
        LIKE,
        EQUALS
    }
}