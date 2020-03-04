package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for table sys_mail_config. Using for saving the email's config.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`sys_mail_config`")
public class SysMailConfig extends BaseDto {
    /**
     * 邮箱服务器地址
     */
    @TableField(value = "`host`")
    private String host;

    /**
     * 邮箱服务器端口
     */
    @TableField(value = "`port`")
    private String port;

    /**
     * 邮箱服务器用户名
     */
    @TableField(value = "`username`")
    private String username;

    /**
     * 邮箱服务器密码
     */
    @TableField(value = "`password`")
    private String password;

}
