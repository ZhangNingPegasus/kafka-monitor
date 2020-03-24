package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for table sys_lag. Using for saving how many messages are backlogged.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`sys_lag`")
public class SysLag extends BaseDto {
    /**
     * 消费者名称
     */
    @TableField(value = "`consumer_name`")
    private String consumerName;

    /**
     * 消费者订阅的主题名称
     */
    @TableField(value = "`topic_name`")
    private String topicName;

    /**
     * 当前消费的偏移量位置
     */
    @TableField(value = "`offset`")
    private Long offset;

    /**
     * 消息堆积数量
     */
    @TableField(value = "`lag`")
    private Long lag;


}
