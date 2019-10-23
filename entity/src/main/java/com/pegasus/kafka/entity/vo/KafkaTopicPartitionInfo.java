package com.pegasus.kafka.entity.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class KafkaTopicPartitionInfo implements Serializable {
    private String topicName;
    private String partitionId;
    private Long logsize;
    private PartionInfo leader;
    private List<PartionInfo> replicas;
    private List<PartionInfo> isr;
    private String strLeader;
    private String strReplicas;
    private String strIsr;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonSerialize
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class PartionInfo implements Serializable {
        private String partitionId;
        private String host;
        private String port;
        private String rack;
    }
}
