package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.entity.dto.SysKpi;
import com.pegasus.kafka.mapper.SysKpiMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * The service for table 'sys_kpi'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysKpiService extends ServiceImpl<SysKpiMapper, SysKpi> {

    @TranRead
    public List<SysKpi> listKpi(List<Integer> kpis, Date from, Date to) {
        QueryWrapper<SysKpi> queryWrapper = new QueryWrapper<>();

        queryWrapper.lambda()
                .in(SysKpi::getKpi, kpis)
                .ge(SysKpi::getCreateTime, from)
                .le(SysKpi::getCreateTime, to)
                .orderByAsc(SysKpi::getCreateTime);

        return this.list(queryWrapper);
    }
}
