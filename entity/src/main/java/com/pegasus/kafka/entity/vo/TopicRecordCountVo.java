package com.pegasus.kafka.entity.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * The View Object for table sys_admin. Using for saving information of administrator.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Data
public class TopicRecordCountVo {
    @TableField(value = "`topic_name`")
    private String topicName;

    @TableField(value = "`log_size`")
    private Long logSize;

    @TableField(value = "`growth_rate`")
    private Double growthRate;
}
