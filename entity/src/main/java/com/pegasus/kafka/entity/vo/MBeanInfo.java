package com.pegasus.kafka.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class MBeanInfo implements Serializable {
    private String name;
    private String oneMinute;
    private String fiveMinute;
    private String fifteenMinute;
    private String meanRate;
    private Double dblOneMinute;
    private Double dblFiveMinute;
    private Double dblFifteenMinute;
    private Double dblMeanRate;
}
