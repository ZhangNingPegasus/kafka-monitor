package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_dingding_config`")
public class SysDingDingConfig extends BaseDto {
    @TableField(value = "`access_token`")
    private String accessToken;

    @TableField(value = "`secret`")
    private String secret;
}
