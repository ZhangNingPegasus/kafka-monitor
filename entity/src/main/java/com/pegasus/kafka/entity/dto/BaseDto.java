package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public abstract class BaseDto implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField(value = "create_time")
    public Date createTime;
}
