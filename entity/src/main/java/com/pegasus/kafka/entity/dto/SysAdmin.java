package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for table sys_admin. Using for saving information of administrator.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_admin`")
public class SysAdmin extends BaseDto {
    /**
     * 角色id(sys_role表的主键)
     */
    @TableField(value = "`sys_role_id`")
    private Long sysRoleId;

    /**
     * 管理员的登陆用户名
     */
    @TableField(value = "`username`")
    private String username;

    /**
     * 管理员的登陆密码
     */
    @TableField(value = "`password`")
    private String password;

    /**
     * 管理员姓名
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 性别(true: 男性; false:女性)
     */
    @TableField(value = "`gender`")
    private Boolean gender;

    /**
     * 手机号码
     */
    @TableField(value = "`phone_number`")
    private String phoneNumber;

    /**
     * 邮件地址
     */
    @TableField(value = "`email`")
    private String email;

    /**
     * 备注
     */
    @TableField(value = "`remark`")
    private String remark;
}
