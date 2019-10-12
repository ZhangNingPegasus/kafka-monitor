package com.pegasus.kafka.entity.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class TopicVo implements Serializable {
    private String name;
    private Integer partitionNum;
    private String partitionIndex;
    private String createTime;
    private String modifyTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPartitionNum() {
        return partitionNum;
    }

    public void setPartitionNum(Integer partitionNum) {
        this.partitionNum = partitionNum;
    }

    public String getPartitionIndex() {
        return partitionIndex;
    }

    public void setPartitionIndex(String partitionIndex) {
        this.partitionIndex = partitionIndex;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }
}
