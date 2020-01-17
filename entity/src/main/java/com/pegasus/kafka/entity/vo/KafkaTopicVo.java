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
    private Double syncLogSizePercent;
    private Long day0LogSize;
    private Long day1LogSize;
    private Long day2LogSize;
    private Long day3LogSize;
    private Long day4LogSize;
    private Long day5LogSize;
    private Long day6LogSize;
    private Integer partitionNum;
    private Integer subscribeNums;
    private String[] subscribeGroupIds;
    private String partitionIndex;
    private String createTime;
    private String modifyTime;
    private Long createTimeLong;
    private Long modifyTimeLong;
    private String error;

    public KafkaTopicVo(String topicName) {
        this.topicName = topicName;
    }

    public KafkaTopicVo(String topicName, Integer consumerStatus) {
        this.topicName = topicName;
        this.consumerStatus = consumerStatus;
    }

    public KafkaTopicVo() {

    }


}
