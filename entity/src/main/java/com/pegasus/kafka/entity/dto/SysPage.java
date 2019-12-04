package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The entity for table sys_page. Using for managing monitor's pages.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_page`")
public class SysPage extends BaseDto {
    /**
     * 页面名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 页面地址
     */
    @TableField(value = "url")
    private String url;

    /**
     * 页面是否出现在菜单栏
     */
    @TableField(value = "is_menu")
    private Boolean isMenu;

    /**
     * 是否是默认页(只允许有一个默认页，如果设置多个，以第一个为准)
     */
    @TableField(value = "is_default")
    private Boolean isDefault;

    /**
     * html中的图标样式
     */
    @TableField(value = "icon_class")
    private String iconClass;

    /**
     * 父级id(即本表的主键id)
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 顺序号(值越小, 排名越靠前)
     */
    @TableField(value = "order_num")
    private Long orderNum;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}