package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.entity.dto.SysPage;
import com.pegasus.kafka.entity.vo.AdminInfo;
import com.pegasus.kafka.entity.vo.PageInfo;
import com.pegasus.kafka.mapper.SysPageMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * The service for table 'sys_page'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysPageService extends ServiceImpl<SysPageMapper, SysPage> {
    @TranRead
    public IPage<PageInfo> list(Integer pageNum,
                                Integer pageSize,
                                @Nullable String name) {
        IPage<PageInfo> page = new Page<>(pageNum, pageSize);
        List<PageInfo> list = this.baseMapper.list(page, name);
        page.setRecords(list);
        return page;
    }

    @TranRead
    public Long getMaxOrderNum(Long parentId) {
        return this.baseMapper.getMaxOrderNum(parentId);
    }
}
