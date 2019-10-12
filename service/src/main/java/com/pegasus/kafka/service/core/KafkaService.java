package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.entity.vo.BrokerVo;
import com.pegasus.kafka.entity.vo.PartitionVo;
import com.pegasus.kafka.entity.vo.TopicPartitionVo;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KafkaService {
    private final KafkaZkService kafkaZkService;
    private final KafkaJmxService kafkaJmxService;

    public KafkaService(KafkaZkService kafkaZkService, KafkaJmxService kafkaJmxService) {
        this.kafkaZkService = kafkaZkService;
        this.kafkaJmxService = kafkaJmxService;
    }

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

    /**
     * 获取指定broker和指定分区id下的主题消息总量
     *
     * @param brokerVo
     * @param topic
     * @param partitionId
     * @return
     */
    public Long getLogSize(BrokerVo brokerVo, String topic, String partitionId) {
        Long result = 0L;
        try {
            result = Long.parseLong(kafkaJmxService.getData(brokerVo, String.format("kafka.log:type=Log,name=LogEndOffset,topic=%s,partition=%s", topic, partitionId), "Value"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Long getLogSize(BrokerVo brokerVo, String topic) {
        Long result = 0L;
        List<TopicPartitionVo> topicPartitionVoList = listTopicParitions(topic);
        for (TopicPartitionVo topicPartitionVo : topicPartitionVoList) {
            if (topicPartitionVo.getLeader().getHost().equals(brokerVo.getHost())) {
                result += getLogSize(brokerVo, topic, topicPartitionVo.getPartitionId());
            }
        }
        return result;
    }

    public List<TopicPartitionVo> listTopicParitions(String topic) {
        final List<TopicPartitionVo> result = new ArrayList<>();
        try {
            KafkaAdminClientDo(kafkaAdminClient -> {
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
        return kafkaZkService.getChildren(Constants.BROKERS_TOPICS_PATH);
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
        kafkaZkService.getData(String.format("%s/%s", Constants.BROKERS_TOPICS_PATH, topicName), stat);
        return stat;
    }

    /**
     * 获取某个主题的所有分区名称
     *
     * @param topicName
     * @return
     * @throws Exception
     */
    public List<String> listPartitionNames(String topicName) throws Exception {
        return kafkaZkService.getChildren(String.format(Constants.BROKERS_TOPICS_PARTITION_PATH, topicName));
    }

    /**
     * 获取某个broker的信息
     *
     * @param brokerName
     * @return
     * @throws Exception
     */
    public BrokerVo getBrokerInfo(String brokerName) throws Exception {
        String brokerInfoJson = kafkaZkService.getData(String.format("%s/%s", Constants.BROKER_IDS_PATH, brokerName));
        JSONObject jsonObject = new JSONObject(brokerInfoJson);
        BrokerVo brokerInfo = new BrokerVo();
        brokerInfo.setHost(jsonObject.get("host").toString());
        brokerInfo.setPort(jsonObject.get("port").toString());
        brokerInfo.setEndpoints(jsonObject.get("endpoints").toString());
        brokerInfo.setJmxPort(jsonObject.get("jmx_port").toString());
        return brokerInfo;
    }

    /**
     * 获取集群中所有的broker名称
     *
     * @return
     * @throws Exception
     */
    private List<String> listBrokerNames() throws Exception {
        return kafkaZkService.getChildren(Constants.BROKER_IDS_PATH);
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

    private void KafkaAdminClientDo(KafkaAdminClientAction kafkaAdminClientAction) throws Exception {
        KafkaAdminClient kafkaAdminClient = null;
        try {
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
            Properties properties = new Properties();
            properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaUrls.toString());
            kafkaAdminClient = (KafkaAdminClient) AdminClient.create(properties);
            kafkaAdminClientAction.action(kafkaAdminClient);
        } finally {
            if (kafkaAdminClient != null) {
                kafkaAdminClient.close();
            }
        }
    }


    private static interface KafkaAdminClientAction {
        void action(KafkaAdminClient kafkaAdminClient) throws Exception;
    }

}
