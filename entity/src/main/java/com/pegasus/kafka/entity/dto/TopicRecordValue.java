package com.pegasus.kafka.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for dynamic table. Using for saving the topic's full content.
 * One topic corresponds one table, the table's name is the name of topic.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class TopicRecordValue extends BaseDto {
    /**
     * 分区id号
     */
    @TableField(value = "partition_id")
    private Integer partitionId;

    /**
     * 消息偏移量
     */
    @TableField(value = "offset")
    private Long offset;

    /**
     * 消息体
     */
    @TableField(value = "value")
    private String value;
}
