package com.pegasus.kafka.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class OffsetInfo implements Serializable {
    private Integer partitionId;
    private Long logSize;
    private Long offset;
    private Long lag;
    private String consumerId;


}
