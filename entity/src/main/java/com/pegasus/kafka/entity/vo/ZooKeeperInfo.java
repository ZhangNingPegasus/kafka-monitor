package com.pegasus.kafka.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ZooKeeperInfo implements Serializable {
    private String host;
    private String port;
    private String mode;
    private String version;
}
