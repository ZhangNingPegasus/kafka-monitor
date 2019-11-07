package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for table sys_alert_consumer. Using for throw a alerm when there's something wrong in consumer.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_alert_consumer`")
public class SysAlertConsumer extends BaseDto {
    @TableField(value = "`group_id`")
    private String groupId;

    @TableField(value = "`topic_name`")
    private String topicName;

    @TableField(value = "`lag_threshold`")
    private Long lagThreshold;

    @TableField(value = "`email`")
    private String email;
}
