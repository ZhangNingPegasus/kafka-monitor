package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The entity for table sys_log_size. Using for saving the kafka topics' log size.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_log_size`")
public class SysLogSize extends BaseDto {
    /**
     * 主题名称
     */
    @TableField(value = "`topic_name`")
    private String topicName;

    /**
     * 主题对应的信息数量
     */
    @TableField(value = "`log_size`")
    private Long logSize;
}