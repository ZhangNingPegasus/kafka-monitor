package com.pegasus.kafka.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class KafkaBrokerInfo implements Serializable {
    private String name;
    private String host;
    private String port;
    private String jmxPort;
    private String endpoints;
    private String version;
    private String createTime;
}