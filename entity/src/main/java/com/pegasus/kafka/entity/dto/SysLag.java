package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
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
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_lag`")
public class SysLag extends BaseDto {
    @TableField(value = "`consumer_name`")
    private String consumerName;

    @TableField(value = "`topic_name`")
    private String topicName;

    @TableField(value = "`lag`")
    private Long lag;
}
