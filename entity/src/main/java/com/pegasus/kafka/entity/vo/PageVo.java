package com.pegasus.kafka.entity.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.pegasus.kafka.entity.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * The View Object for table sys_page. Using for managing monitor's pages.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PageVo extends BaseDto {
    @TableField("name")
    private String name;

    @TableField("url")
    private String url;

    @TableField("is_menu")
    private Boolean isMenu;

    @TableField(value = "`is_default`")
    private Boolean isDefault;

    @TableField(value = "is_blank")
    private Boolean isBlank;

    @TableField("icon_class")
    private String iconClass;

    @TableField("parent_id")
    private Long parentId;

    @TableField("parent_name")
    private String parentName;

    @TableField("order_num")
    private Long orderNum;

    @TableField("remark")
    private String remark;

    @TableField("can_insert")
    private Boolean canInsert;

    @TableField("can_delete")
    private Boolean canDelete;

    @TableField("can_update")
    private Boolean canUpdate;

    @TableField("can_select")
    private Boolean canSelect;

    @TableField(exist = false)
    private List<PageVo> children;
}