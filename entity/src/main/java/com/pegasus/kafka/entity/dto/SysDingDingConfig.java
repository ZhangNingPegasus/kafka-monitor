package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for table sys_dingding_config. Using for dingding's config.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_dingding_config`")
public class SysDingDingConfig extends BaseDto {
    /**
     * 钉钉机器人的access_token
     */
    @TableField(value = "`access_token`")
    private String accessToken;

    /**
     * 钉钉机器人的加签秘钥
     */
    @TableField(value = "`secret`")
    private String secret;
}
