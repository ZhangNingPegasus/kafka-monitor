package com.pegasus.kafka.entity.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.pegasus.kafka.entity.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * The View Object for table sys_admin. Using for saving information of administrator.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AdminInfo extends BaseDto {
    @TableField(value = "`role_id`")
    private Long roleId;

    @TableField(value = "`role_name`")
    private String roleName;

    @TableField(value = "`username`")
    private String username;

    @TableField(value = "`name`")
    private String name;

    @TableField(value = "`gender`")
    private Boolean gender;

    @TableField(value = "`phone_number`")
    private String phoneNumber;

    @TableField(value = "`email`")
    private String email;

    @TableField(value = "`remark`")
    private String remark;
}
