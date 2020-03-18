package com.pegasus.kafka.service.record;


import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.po.Topic;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.core.ThreadService;
import com.pegasus.kafka.service.property.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * The service for Kafka's topics' thread schedule.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class CoreService implements SmartLifecycle, DisposableBean {
    private static final int TOPIC_NUMBER_FACTOR = 1024;
    private static final Logger logger = LoggerFactory.getLogger(CoreService.class);
    private final ConfigurableApplicationContext applicationContext;
    private final KafkaService kafkaService;
    private final ThreadService threadService;
    private final List<String> blacklist;
    private List<String> topicNames;
    private boolean running;
    private Map<String, Topic> beanNameTopicMap;

    public CoreService(ConfigurableApplicationContext applicationContext,
                       KafkaService kafkaService,
                       ThreadService threadService,
                       PropertyService propertyService) {
        this.applicationContext = applicationContext;
        this.kafkaService = kafkaService;
        this.threadService = threadService;
        this.topicNames = new ArrayList<>(TOPIC_NUMBER_FACTOR);
        this.beanNameTopicMap = new HashMap<>(TOPIC_NUMBER_FACTOR);
        this.blacklist = new ArrayList<>(propertyService.getTopicBlackList().split(",").length);
    }

    private void installTopic(Topic topic) {
        uninstallTopic(topic);
        String beanName = getBeanName(topic);
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        genericApplicationContext.registerBean(beanName, RecordService.class, topic);
        RecordService recordService = genericApplicationContext.getBean(beanName, RecordService.class);
        this.beanNameTopicMap.put(beanName, topic);
        if (!recordService.isRunning()) {
            recordService.start();
        }
    }

    public void uninstallTopic(Topic topic) {
        if (containsTopicName(topic)) {
            String beanName = getBeanName(topic);
            GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
            RecordService recordService = genericApplicationContext.getBean(beanName, RecordService.class);
            if (recordService.isRunning()) {
                recordService.stop();
                genericApplicationContext.removeBeanDefinition(beanName);
            }
            this.beanNameTopicMap.remove(beanName);
        }
    }


    public void uninstallTopicName(String topicName) {
        boolean exit;
        Topic topic;
        for (Map.Entry<String, Topic> pair : this.beanNameTopicMap.entrySet()) {
            String beanName = pair.getKey();
            topic = pair.getValue();
            exit = false;
            for (String s : topic.getTopicNameList()) {
                if (s.equals(topicName)) {
                    uninstallTopic(topic);
                    exit = true;
                    break;
                }
            }
            if (exit) {
                break;
            }
        }
    }

    public boolean containsTopicName(Topic topic) {
        String beanName = getBeanName(topic);
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        return genericApplicationContext.containsBeanDefinition(beanName);
    }

    private String getBeanName(Topic topic) {
        return String.format("%s#%s", RecordService.class, topic.getName());
    }

    @Override
    public void start() {
        threadService.submit(() -> {
            Thread.currentThread().setName("thread-new-topic-detect");
            setRunning(true);

            while (isRunning()) {
                try {
                    Set<String> currentTopicNames = getTopicsFromKafka();
                    if (currentTopicNames.size() > 0) {

                        this.topicNames.clear();
                        this.topicNames.addAll(getSubscribeTopicNames());

                        if (!this.topicNames.containsAll(currentTopicNames) || !currentTopicNames.containsAll(this.topicNames)) {
                            List<Topic> topicList = convert(new ArrayList<>(currentTopicNames));
                            if (topicList != null) {
                                stopTheWorld();
                                for (Topic topic : topicList) {
                                    installTopic(topic);
                                }
                            }
                        }
                    }

                    try {
                        Thread.sleep(10000);
                    } catch (Exception ignored) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Set<String> getTopicsFromKafka() throws Exception {
        Set<String> result = new HashSet<>();
        for (String topicName : kafkaService.listTopicNames()) {
            if (!blacklist.contains(topicName)) {
                result.add(topicName);
            }
        }
        return result;
    }

    private Set<String> getSubscribeTopicNames() {
        Set<String> result = new HashSet<>(TOPIC_NUMBER_FACTOR);
        for (String beanName : this.beanNameTopicMap.keySet()) {
            GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
            RecordService recordService = genericApplicationContext.getBean(beanName, RecordService.class);
            result.addAll(recordService.getTopicsNames());
        }
        return result;
    }

    private void stopTheWorld() {
        List<Topic> topicList = new ArrayList<>(this.beanNameTopicMap.values());
        for (Topic value : topicList) {
            uninstallTopic(value);
        }
    }

    private List<Topic> convert(List<String> topicNames) {
        if (topicNames.size() < 1) {
            return null;
        }

        topicNames.sort(String::compareTo);
        int i = 0;
        List<List<String>> averageTopicNames = Common.averageAssign(topicNames, 10);
        List<Topic> result = new ArrayList<>();
        for (List<String> averageTopicNameList : averageTopicNames) {
            if (averageTopicNameList == null || averageTopicNameList.size() < 1) {
                continue;
            }
            Topic topic = new Topic();
            topic.setName(String.valueOf(i));
            topic.setTopicNameList(averageTopicNameList);
            result.add(topic);
            i++;
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