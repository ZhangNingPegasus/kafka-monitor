package com.pegasus.kafka.service.kafka;


import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.dto.TopicRecordService;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * The service for Kafka's topics' records.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class KafkaRecordService implements SmartLifecycle {
    public static final Integer BATCH_SIZE = 2048;
    private static final Logger log = LoggerFactory.getLogger(KafkaRecordService.class);
    private static final Integer durationTimeout = 1000;
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;
    private ArrayBlockingQueue<TopicRecord> topicRecords;
    private AtomicLong discardCount;
    private Thread worker;
    private boolean running;


    public KafkaRecordService(KafkaService kafkaService, TopicRecordService topicRecordService) {
        this.kafkaService = kafkaService;
        this.topicRecords = new ArrayBlockingQueue<>(BATCH_SIZE * 2048);
        this.discardCount = new AtomicLong(0L);
        this.topicRecordService = topicRecordService;
    }


    @Override
    public void start() {
        if (!this.isRunning()) {
            new Thread(() -> {
                KafkaConsumer<String, String> consumer = null;
                try {
                    Properties properties = new Properties();
                    properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer());
                    properties.setProperty("group.id", Constants.KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MESSAGE);
                    properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
                    properties.setProperty("enable.auto.commit", "true");
                    properties.setProperty("auto.commit.interval.ms", "1000");
                    properties.setProperty("isolation.level", "read_committed");
                    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                    properties.setProperty("key.deserializer", StringDeserializer.class.getCanonicalName());
                    properties.setProperty("value.deserializer", StringDeserializer.class.getCanonicalName());
                    consumer = new KafkaConsumer<>(properties);
                    consumer.subscribe(Pattern.compile("([\\w\\W]*)"));
                    setRunning(true);
                    this.worker = new Thread(new AsyncRunnable(), "Kafka_Monitor_Trace_Record_Thread-" + UUID.randomUUID().toString());
                    this.worker.setDaemon(true);
                    this.worker.start();
                    while (isRunning()) {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(durationTimeout));
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
                                log.info("buffer full" + discardCount.incrementAndGet() + " ,context is " + topicRecord);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (consumer != null) {
                        consumer.close();
                    }
                }
            }).start();
        }
    }

    @Override
    public void stop() {
        setRunning(false);
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
                for (Integer i = 0; i < BATCH_SIZE; i++) {
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
                        topicRecordService.batchSave(topicRecordList);
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
