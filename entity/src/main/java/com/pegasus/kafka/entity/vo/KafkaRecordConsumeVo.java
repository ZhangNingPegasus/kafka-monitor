package com.pegasus.kafka.entity.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

/**
 * The view object for ajax's response. Using for show the kafka's consumer's topic status information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class KafkaRecordConsumeVo implements Serializable {
    private String groupId;
    private Integer isConsume;
}
