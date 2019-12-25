package com.pegasus.kafka.service.record;


import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.core.ThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The service for Kafka's topics' records.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class KafkaRecordService implements SmartLifecycle, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(KafkaRecordService.class);
    private final ConfigurableApplicationContext applicationContext;
    private final KafkaService kafkaService;
    private final ThreadService threadService;
    private List<String> topicNames;
    private boolean running;

    public KafkaRecordService(ConfigurableApplicationContext applicationContext, KafkaService kafkaService, ThreadService threadService) {
        this.applicationContext = applicationContext;
        this.kafkaService = kafkaService;
        this.threadService = threadService;
        this.topicNames = new ArrayList<>(1024);
    }

    private void installTopic(String topicName) {
        if (!containsTopicName(topicName)) {
            String beanName = getBeanName(topicName);
            GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
            genericApplicationContext.registerBean(beanName, KafkaTopicRecord.class, topicName);
            KafkaTopicRecord kafkaTopicRecord = genericApplicationContext.getBean(beanName, KafkaTopicRecord.class);
            if (!kafkaTopicRecord.isRunning()) {
                kafkaTopicRecord.start();
            }
            logger.info(String.format("[%s] : topic [%s] is beginning to collect.", kafkaTopicRecord, topicName));
        }
    }

    public void uninstallTopic(String topicName) {
        if (containsTopicName(topicName)) {
            String beanName = getBeanName(topicName);
            GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
            KafkaTopicRecord kafkaTopicRecord = genericApplicationContext.getBean(beanName, KafkaTopicRecord.class);
            genericApplicationContext.removeBeanDefinition(beanName);
        }
    }

    public boolean containsTopicName(String topicName) {
        String beanName = getBeanName(topicName);
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        return genericApplicationContext.containsBeanDefinition(beanName);
    }

    private String getBeanName(String topicName) {
        return String.format("%s#%s", KafkaTopicRecord.class, topicName);
    }

    @Override
    public void start() {
        threadService.submit(() -> {
            setRunning(true);
            while (isRunning()) {
                try {
                    List<String> currentTopicNames = kafkaService.listTopicNames();
                    List<String> _topicNames = new ArrayList<>();
                    for (String currentTopicName : currentTopicNames) {
                        if (!this.topicNames.contains(currentTopicName)) {
                            _topicNames.add(currentTopicName);
                        }
                    }

                    this.topicNames.clear();
                    this.topicNames = currentTopicNames;

                    GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
                    for (String topicName : _topicNames) {
                        installTopic(topicName);
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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

    @Override
    public void destroy() {
        stop();
    }


}
