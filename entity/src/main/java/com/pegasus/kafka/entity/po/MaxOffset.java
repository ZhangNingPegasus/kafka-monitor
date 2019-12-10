package com.pegasus.kafka.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class MaxOffset {
    @TableField(value = "partition_id")
    private Integer partitionId;

    @TableField(value = "offset")
    private Long offset;
}
