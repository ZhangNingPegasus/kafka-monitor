package com.pegasus.kafka.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pegasus.kafka.entity.dto.SysKpi;
import com.pegasus.kafka.entity.dto.SysLag;
import org.springframework.stereotype.Repository;

@Repository
public interface SysKpiMapper extends BaseMapper<SysKpi> {

}
