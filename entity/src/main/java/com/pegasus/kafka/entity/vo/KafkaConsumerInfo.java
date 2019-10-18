package com.pegasus.kafka.entity.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class KafkaConsumerInfo implements Serializable {
    private String groupId;
    private String node;
    private Integer topicCount;
    private Integer activeTopicCount;
    private Set<String> topicNames;
    private Set<String> activeTopicNames;
    private Set<String> notActiveTopicNames;
    private List<Meta> metaList;


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public List<Meta> getMetaList() {
        return metaList;
    }

    public void setMetaList(List<Meta> metaList) {
        this.metaList = metaList;
    }

    public Integer getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(Integer topicCount) {
        this.topicCount = topicCount;
    }

    public Set<String> getTopicNames() {
        return topicNames;
    }

    public void setTopicNames(Set<String> topicNames) {
        this.topicNames = topicNames;
    }

    public Set<String> getActiveTopicNames() {
        return activeTopicNames;
    }

    public void setActiveTopicNames(Set<String> activeTopicNames) {
        this.activeTopicNames = activeTopicNames;
    }

    public Set<String> getNotActiveTopicNames() {
        return notActiveTopicNames;
    }

    public void setNotActiveTopicNames(Set<String> notActiveTopicNames) {
        this.notActiveTopicNames = notActiveTopicNames;
    }

    public Integer getActiveTopicCount() {
        return activeTopicCount;
    }

    public void setActiveTopicCount(Integer activeTopicCount) {
        this.activeTopicCount = activeTopicCount;
    }

    public static class Meta {
        private String consumerId;
        private String node;
        private List<TopicSubscriber> topicSubscriberList;

        public String getConsumerId() {
            return consumerId;
        }

        public void setConsumerId(String consumerId) {
            this.consumerId = consumerId;
        }

        public String getNode() {
            return node;
        }

        public void setNode(String node) {
            this.node = node;
        }

        public List<TopicSubscriber> getTopicSubscriberList() {
            return topicSubscriberList;
        }

        public void setTopicSubscriberList(List<TopicSubscriber> topicSubscriberList) {
            this.topicSubscriberList = topicSubscriberList;
        }

    }

    public static class TopicSubscriber {
        private String topicName;
        private Integer partitionId;

        public String getTopicName() {
            return topicName;
        }

        public void setTopicName(String topicName) {
            this.topicName = topicName;
        }

        public Integer getPartitionId() {
            return partitionId;
        }

        public void setPartitionId(Integer partitionId) {
            this.partitionId = partitionId;
        }

    }

}
