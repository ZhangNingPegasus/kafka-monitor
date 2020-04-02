package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for table sys_alert_topic. Using for throw a alerm when there's something wrong in topic.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         27/3/2020      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`sys_alert_topic`")
public class SysAlertTopic extends BaseDto {
    @TableField(value = "`topic_name`")
    private String topicName;

    @TableField(value = "`from_time`")
    private String fromTime;

    @TableField(value = "`to_time`")
    private String toTime;

    @TableField(value = "`from_tps`")
    private Integer fromTps;

    @TableField(value = "`to_tps`")
    private Integer toTps;

    @TableField(value = "`from_mom_tps`")
    private Integer fromMomTps;

    @TableField(value = "`to_mom_tps`")
    private Integer toMomTps;

    @TableField(value = "`email`")
    private String email;

    public String toInfo() {
        return String.format("TPS范围: [%s, %s]; 变化范围: [%s, %s]", this.fromTps, this.toTps, this.fromMomTps, this.toMomTps);
    }

    public String toTpsInfo() {
        return String.format("[%s, %s]", this.fromTps, this.toTps);
    }

    public String toMomTpsInfo() {
        return String.format("[%s, %s]", this.fromMomTps, this.toMomTps);
    }
}
