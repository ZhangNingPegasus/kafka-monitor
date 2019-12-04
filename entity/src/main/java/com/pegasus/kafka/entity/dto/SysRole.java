package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for table sys_role. Using for saving information of role.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_role`")
public class SysRole extends BaseDto {
    /**
     * 角色名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 是否是超级管理员(true:是; false:否)
     */
    @TableField(value = "`super_admin`")
    private Boolean superAdmin;

    /**
     * 角色说明
     */
    @TableField(value = "`remark`")
    private String remark;
}
