package com.pegasus.kafka.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

/**
 * The view object for ajax's response. Using for show the kafka's topics information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class KafkaTopicVo implements Serializable {
    private String topicName;
    private Integer consumerStatus;
    private Long lag;
    private Long logSize;
    private Long syncLogSize;
    private Long todayLogSize;
    private Long yesterdayLogSize;
    private Long tdbyLogSize;
    private Integer partitionNum;
    private Integer subscribeNums;
    private String[] subscribeGroupIds;
    private String partitionIndex;
    private String createTime;
    private String modifyTime;
    private Long createTimeLong;
    private Long modifyTimeLong;
    private String error;

    public KafkaTopicVo(String topicName, Integer consumerStatus) {
        this.topicName = topicName;
        this.consumerStatus = consumerStatus;
    }

    public KafkaTopicVo() {

    }


}
