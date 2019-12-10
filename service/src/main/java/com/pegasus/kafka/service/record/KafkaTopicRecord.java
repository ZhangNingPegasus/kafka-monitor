package com.pegasus.kafka.service.record;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.entity.po.MaxOffset;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.dto.TopicRecordService;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class KafkaTopicRecord implements InitializingBean, SmartLifecycle, DisposableBean {
    public static final Integer BATCH_SIZE = 2048;
    private static final Logger log = LoggerFactory.getLogger(KafkaRecordService.class);
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;
    private boolean running;
    private String topicName;
    private String consumerGroupdId;
    private Properties properties;
    private KafkaConsumer<String, String> kafkaConsumer = null;
    private ArrayBlockingQueue<TopicRecord> topicRecords;
    private AtomicLong discardCount;
    private Thread worker;

    public KafkaTopicRecord(String topicName, KafkaService kafkaService, TopicRecordService topicRecordService) {
        this.topicName = topicName;
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
        this.topicRecords = new ArrayBlockingQueue<>(BATCH_SIZE * 2048);
        this.discardCount = new AtomicLong(0L);
        this.consumerGroupdId = String.format("%s__%s", Constants.KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MESSAGE, this.topicName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        properties = new Properties();
        properties.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, this.consumerGroupdId);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        properties.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void start() {
        new Thread(() -> {
            try {
                List<MaxOffset> maxOffsetList = topicRecordService.getMaxOffset(this.topicName);
                kafkaConsumer = new KafkaConsumer<>(properties);
                if (maxOffsetList != null && maxOffsetList.size() > 0) {
                    kafkaConsumer.assign(maxOffsetList.stream().map(p -> new TopicPartition(this.topicName, p.getPartitionId())).collect(Collectors.toList()));
                    for (MaxOffset maxOffset : maxOffsetList) {
                        TopicPartition p = new TopicPartition(this.topicName, maxOffset.getPartitionId());
                        kafkaConsumer.seek(p, maxOffset.getOffset() + 1);
                    }
                } else {
                    kafkaConsumer.subscribe(Collections.singleton(this.topicName));
                }
                setRunning(true);
                this.worker = new Thread(new AsyncRunnable(), String.format("Kafka_Monitor_Trace_Record_Thread_%s_%s", this.topicName, UUID.randomUUID().toString()));
                this.worker.setDaemon(true);
                this.worker.start();
                while (isRunning()) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord<String, String> record : records) {
                        TopicRecord topicRecord = new TopicRecord();
                        topicRecord.setTopicName(record.topic());
                        topicRecord.setPartitionId(record.partition());
                        topicRecord.setOffset(record.offset());
                        topicRecord.setKey(record.key());
                        topicRecord.setValue(record.value());
                        topicRecord.setTimestamp(new Date(record.timestamp()));
                        boolean result = topicRecords.offer(topicRecord);
                        if (!result) {
                            log.info(String.format("buffer full for topic [%s], [%s], conent is [%s]", this.topicName, discardCount.incrementAndGet(), topicRecord));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (kafkaConsumer != null) {
                    kafkaConsumer.close();
                    kafkaConsumer = null;
                    log.info(String.format("[%s] : topic [%s] is stopping to collect.", this, topicName));
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        setRunning(false);
        while (kafkaConsumer != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            kafkaService.deleteConsumerGroups(consumerGroupdId);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }


    private void setRunning(boolean running) {
        this.running = running;
    }

    private class AsyncRunnable implements Runnable {

        @Override
        public void run() {

            while (true) {
                List<TopicRecord> topicRecordList = new ArrayList<>(BATCH_SIZE);
                for (int i = 0; i < BATCH_SIZE; i++) {
                    TopicRecord topicRecord = null;
                    try {
                        topicRecord = topicRecords.poll(5, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignore) {
                    }
                    if (topicRecord == null) {
                        break;
                    }
                    topicRecordList.add(topicRecord);
                }

                if (topicRecordList.size() > 0) {
                    try {
                        topicRecordService.batchSave(topicName, topicRecordList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!isRunning() && topicRecords.size() < 1) {
                    break;
                }

            }
        }
    }

}
