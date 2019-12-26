package com.pegasus.kafka.service.record;


import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.po.Topic;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.core.ThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private static final int TOPIC_NUMBER_FACTOR = 1024;
    private static final Logger logger = LoggerFactory.getLogger(KafkaRecordService.class);
    private final ConfigurableApplicationContext applicationContext;
    private final KafkaService kafkaService;
    private final ThreadService threadService;
    private List<String> topicNames;
    private boolean running;
    private Map<String, Topic> topicBeanMap;

    public KafkaRecordService(ConfigurableApplicationContext applicationContext, KafkaService kafkaService, ThreadService threadService) {
        this.applicationContext = applicationContext;
        this.kafkaService = kafkaService;
        this.threadService = threadService;
        this.topicNames = new ArrayList<>(TOPIC_NUMBER_FACTOR);
        this.topicBeanMap = new HashMap<>(TOPIC_NUMBER_FACTOR);
    }

    private void installTopic(Topic topic) {
        uninstallTopic(topic);
        String beanName = getBeanName(topic);
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        genericApplicationContext.registerBean(beanName, KafkaTopicRecord.class, topic);
        KafkaTopicRecord kafkaTopicRecord = genericApplicationContext.getBean(beanName, KafkaTopicRecord.class);
        this.topicBeanMap.put(beanName, topic);
        if (!kafkaTopicRecord.isRunning()) {
            kafkaTopicRecord.start();
        }
    }

    private void uninstallTopic(Topic topic) {
        if (containsTopicName(topic)) {
            String beanName = getBeanName(topic);
            GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
            KafkaTopicRecord kafkaTopicRecord = genericApplicationContext.getBean(beanName, KafkaTopicRecord.class);
            genericApplicationContext.removeBeanDefinition(beanName);
            this.topicBeanMap.remove(beanName);
        }
    }

    public void uninstallTopicName(String topicName) {
        boolean exit;
        Topic topic = null;
        for (Map.Entry<String, Topic> pair : this.topicBeanMap.entrySet()) {
            String beanName = pair.getKey();
            topic = pair.getValue();
            exit = false;
            for (String s : topic.getTopicNameList()) {
                if (s.equals(topicName)) {
                    uninstallTopic(topic);
                    //iterator.remove();
                    exit = true;
                    break;
                }
            }

            if (exit) {
                break;
            }
        }

        if (topic != null && topic.getTopicNameList().size() > 0) {
            Topic newTopic = new Topic();
            newTopic.setName(topic.getName());
            List<String> topics = new ArrayList<>(topic.getTopicNameList().size() - 1);
            for (String name : topic.getTopicNameList()) {
                if (!name.equals(topicName)) {
                    topics.add(name);
                }
            }
            if (newTopic.getTopicNameList() != null && newTopic.getTopicNameList().size() > 0) {
                installTopic(newTopic);
            }
        }

    }

    public boolean containsTopicName(Topic topic) {
        String beanName = getBeanName(topic);
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        return genericApplicationContext.containsBeanDefinition(beanName);
    }

    private String getBeanName(Topic topic) {
        return String.format("%s#%s", KafkaTopicRecord.class, topic.getName());
    }

    @Override
    public void start() {
        threadService.submit(() -> {
            setRunning(true);
            while (isRunning()) {
                try {
                    this.topicNames.clear();
                    this.topicNames.addAll(getSubscribeTopicNames());

                    List<String> currentTopicNames = kafkaService.listTopicNames();
                    List<String> newFoundTopicNames = new ArrayList<>();
                    for (String currentTopicName : currentTopicNames) {
                        if (!this.topicNames.contains(currentTopicName)) {
                            newFoundTopicNames.add(currentTopicName);
                        }
                    }

                    List<Topic> topicList = convert(newFoundTopicNames);
                    if (topicList != null && topicList.size() > 0) {
                        for (Topic topic : topicList) {
                            installTopic(topic);
                        }
                    }

                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Set<String> getSubscribeTopicNames() {
        Set<String> result = new HashSet<>(TOPIC_NUMBER_FACTOR);
        for (String beanName : this.topicBeanMap.keySet()) {
            GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
            KafkaTopicRecord bean = genericApplicationContext.getBean(beanName, KafkaTopicRecord.class);
            result.addAll(bean.getTopicsNames());
        }
        return result;
    }

    private List<Topic> convert(List<String> topicNames) {
        if (topicNames.size() < 1) {
            return null;
        }

        topicNames.sort(String::compareTo);

        List<List<String>> averageTopicNames = Common.averageAssign(topicNames, 100);
        int i = 0;
        List<Topic> result = new ArrayList<>();
        for (List<String> averageTopicNameList : averageTopicNames) {
            if (averageTopicNameList == null || averageTopicNameList.size() < 1) {
                continue;
            }
            Topic topic = new Topic();
            topic.setName(String.valueOf(i++));
            topic.setTopicNameList(averageTopicNameList);
            result.add(topic);
        }
        return result;
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
