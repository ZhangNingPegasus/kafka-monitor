package com.pegasus.kafka.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class PartitionVo implements Serializable {
    private String partitionId;
    private String host;
    private String port;
    private String rack;

    public PartitionVo() {

    }

    public PartitionVo(String partitionId, String host, String port, String rack) {
        this.partitionId = partitionId;
        this.host = host;
        this.port = port;
        this.rack = rack;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    @Override
    public String toString() {
        return "PartitionVo{" +
                "partitionId='" + partitionId + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", rack='" + rack + '\'' +
                '}';
    }
}
