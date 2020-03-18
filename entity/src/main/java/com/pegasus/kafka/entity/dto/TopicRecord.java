package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.vo.KafkaTopicRecordVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * The entity for dynamic table. Using for saving the topic's content.
 * One topic corresponds one table, the table's name is the name of topic.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Data
public class TopicRecord extends BaseDto {
    /**
     * 主题名称（非数据库字段）
     */
    @TableField(exist = false)
    private String topicName;

    /**
     * 分区号
     */
    @TableField(value = "partition_id")
    private Integer partitionId;

    /**
     * 消息在该分区号下的偏移量
     */
    @TableField(value = "offset")
    private Long offset;

    /**
     * 消息键值
     */
    @TableField(value = "key")
    private String key;

    /**
     * 消息体的缩微样本
     */
    @TableField(value = "value")
    private String value;

    /**
     * 消息时间
     */
    @TableField(value = "timestamp")
    private Date timestamp;

    public KafkaTopicRecordVo toVo() {
        KafkaTopicRecordVo result = new KafkaTopicRecordVo();
        result.setTopicName(this.getTopicName());
        result.setPartitionId(this.getPartitionId().toString());
        result.setOffset(this.getOffset().toString());
        result.setKey(this.getKey());
        result.setCreateTime(Common.format(this.getTimestamp()));
        result.setValue(this.getValue());
        result.setTimestamp(this.getTimestamp().getTime());
        return result;
    }
}
