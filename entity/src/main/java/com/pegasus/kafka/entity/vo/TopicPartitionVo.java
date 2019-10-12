package com.pegasus.kafka.entity.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.List;

@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class TopicPartitionVo implements Serializable {
    private String topicName;
    private String partitionId;
    private PartitionVo leader;
    private List<PartitionVo> replicas;
    private List<PartitionVo> isr;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public PartitionVo getLeader() {
        return leader;
    }

    public void setLeader(PartitionVo leader) {
        this.leader = leader;
    }

    public List<PartitionVo> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<PartitionVo> replicas) {
        this.replicas = replicas;
    }

    public List<PartitionVo> getIsr() {
        return isr;
    }

    public void setIsr(List<PartitionVo> isr) {
        this.isr = isr;
    }
}
