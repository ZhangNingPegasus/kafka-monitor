package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * The entity for table sys_alert_cluster. Using for throw a alerm when there's something wrong in cluster.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_alert_cluster`")
public class SysAlertCluster extends BaseDto {
    @TableField(value = "`type`")
    private Integer type;

    @TableField(value = "`server`")
    private String server;

    @TableField(value = "`email`")
    private String email;

    @Getter
    public enum Type {
        ZOOKEEPER(1, "ZOOKEEPER"),
        KAFKA(2, "KAFKA");

        private int code;
        private String description;

        Type(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static Type get(Integer code) {
            for (Type item : Type.values()) {
                if (item.getCode() == code) {
                    return item;
                }
            }
            return null;
        }

        public static Type get(String description) {
            for (Type item : Type.values()) {
                if (item.getDescription().equals(description)) {
                    return item;
                }
            }
            return null;
        }
    }
}
