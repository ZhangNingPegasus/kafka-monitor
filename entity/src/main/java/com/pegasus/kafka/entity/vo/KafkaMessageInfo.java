package com.pegasus.kafka.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class KafkaMessageInfo implements Serializable {

    private String topicName;
    private String partitionId;
    private String offset;
    private String key;
    private String createTime;
    private String value;
    private Long timestamp;
}
