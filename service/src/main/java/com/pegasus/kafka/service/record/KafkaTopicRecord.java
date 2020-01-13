package com.pegasus.kafka.service.record;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.entity.po.MaxOffset;
import com.pegasus.kafka.entity.po.Topic;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.core.ThreadService;
import com.pegasus.kafka.service.dto.TopicRecordService;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KafkaTopicRecord implements SmartLifecycle, DisposableBean {
    public static final Integer BATCH_SIZE = 2048;
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopicRecord.class);
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;
    private final ThreadService threadService;
    private final KafkaConsumerService kafkaConsumerService;
    private boolean running;
    private Topic topic;
    private String consumerGroupdId;
    private CountDownLatch cdl;

    public KafkaTopicRecord(Topic topic, KafkaService kafkaService, TopicRecordService topicRecordService, ThreadService threadService, KafkaConsumerService kafkaConsumerService) {
        this.topic = topic;
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
        this.threadService = threadService;
        this.kafkaConsumerService = kafkaConsumerService;
        this.consumerGroupdId = String.format("%s_%s", Constants.KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MESSAGE, this.topic.getName());
    }

    public List<String> getTopicsNames() {
        return this.topic.getTopicNameList();
    }

    public String getConsumerGroupdId() {
        return this.consumerGroupdId;
    }

    @Override
    public void start() {
        cdl = new CountDownLatch(1);

        threadService.submit(() -> {
            Thread.currentThread().setName(String.format("thread-data-sync-%s", this.topic.getName()));

            try {
                List<String> groupIdList = kafkaConsumerService.listAllConsumers();
                for (String groupId : groupIdList) {
                    try {
                        if (groupId.startsWith(Constants.KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX)) {
                            kafkaConsumerService.delete(groupId);
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }

            KafkaConsumer<String, String> kafkaConsumer = null;
            try {

                Properties properties = new Properties();
                properties.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer());
                properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, this.consumerGroupdId);
                properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
                properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
                properties.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, String.valueOf(1000 * 60));
                properties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, String.valueOf(1000 * 60));
                properties.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
                properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
                properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());

                Map<String, List<MaxOffset>> maxOffsetMap = topicRecordService.listMaxOffset(this.topic.getTopicNameList());
                kafkaConsumer = new KafkaConsumer<>(properties);

                if (maxOffsetMap == null || maxOffsetMap.size() < 1) {
                    kafkaConsumer.subscribe(this.topic.getTopicNameList());
                } else {
                    List<TopicPartition> topicPartitionList = new ArrayList<>(maxOffsetMap.size());
                    maxOffsetMap.forEach((topicName, maxOffsetList) -> {
                        for (MaxOffset maxOffset : maxOffsetList) {
                            topicPartitionList.add(new TopicPartition(topicName, maxOffset.getPartitionId()));
                        }
                    });
                    kafkaConsumer.assign(topicPartitionList);

                    for (Map.Entry<String, List<MaxOffset>> pair : maxOffsetMap.entrySet()) {
                        String topicName = pair.getKey();
                        List<MaxOffset> maxOffsetList = pair.getValue();
                        for (MaxOffset maxOffset : maxOffsetList) {
                            TopicPartition topicPartition = new TopicPartition(topicName, maxOffset.getPartitionId());
                            kafkaConsumer.seek(topicPartition, maxOffset.getOffset() + 1L);
                        }
                    }
                }

                setRunning(true);

                for (String topicName : this.topic.getTopicNameList()) {
                    logger.info(String.format("[%s] : topic [%s] is beginning to collect.", Thread.currentThread().getName(), topicName));
                }

                while (isRunning()) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(500));
                    List<TopicRecord> topicRecordList = new ArrayList<>((int) (records.count() / 0.75));

                    for (ConsumerRecord<String, String> record : records) {
                        TopicRecord topicRecord = new TopicRecord();
                        topicRecord.setTopicName(record.topic());
                        topicRecord.setPartitionId(record.partition());
                        topicRecord.setOffset(record.offset());
                        topicRecord.setKey(record.key());
                        topicRecord.setValue(record.value());
                        topicRecord.setTimestamp(new Date(record.timestamp()));
                        topicRecordList.add(topicRecord);
                    }
                    topicRecordService.batchSave(topicRecordList);

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (kafkaConsumer != null) {
                    kafkaConsumer.close();
                    logger.info(String.format("[%s] : topic [%s] is stopping to collect.", Thread.currentThread().getName(), this.topic.getName()));
                }
                cdl.countDown();
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        setRunning(false);
        cdl.await(10, TimeUnit.SECONDS);
        try {
            kafkaService.deleteConsumerGroups(consumerGroupdId);
        } catch (Exception e) {
        }

    }

    @Override
    public void stop() {
        try {
            destroy();
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }


    private void setRunning(boolean running) {
        this.running = running;
    }

}