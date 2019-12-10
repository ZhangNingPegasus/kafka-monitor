package com.pegasus.kafka.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pegasus.kafka.entity.dto.SysLag;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The mapper for table 'sys_lag'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Repository
public interface SysLagMapper extends BaseMapper<SysLag> {

    List<SysLag> listTopLag(@Param(value = "top") int top);

    void batchSave(@Param("sysLagList") List<SysLag> sysLagList);
}
