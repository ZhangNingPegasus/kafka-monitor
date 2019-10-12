package com.pegasus.kafka.service.kafka;

import com.pegasus.kafka.entity.vo.BrokerVo;
import com.pegasus.kafka.entity.vo.TopicVo;
import com.pegasus.kafka.service.core.KafkaService;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class KafkaTopicService {
    private final KafkaService kafkaService;

    public KafkaTopicService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public List<TopicVo> getAllTopics() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<TopicVo> topicInfoList = new ArrayList<>();
        List<String> topicNameList = kafkaService.listTopicNames();
        for (String topicName : topicNameList) {
            Stat stat = kafkaService.getTopicStat(topicName);
            List<String> partitionList = kafkaService.listPartitionNames(topicName);
            TopicVo topicInfo = new TopicVo();
            topicInfo.setName(topicName);
            topicInfo.setPartitionNum(partitionList.size());
            topicInfo.setPartitionIndex(partitionList.toString());
            topicInfo.setCreateTime(df.format(new Date(stat.getCtime())));
            topicInfo.setModifyTime(df.format(new Date(stat.getMtime())));
            topicInfoList.add(topicInfo);
        }

        List<BrokerVo> brokerInfoList = kafkaService.listBrokerInfos();
        System.out.println(brokerInfoList);
        System.out.println(kafkaService.getLogSize(brokerInfoList.get(0), "mn"));
        return topicInfoList;
    }

}
