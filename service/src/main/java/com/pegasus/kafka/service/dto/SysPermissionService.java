package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.entity.dto.SysPermission;
import com.pegasus.kafka.entity.vo.PageInfo;
import com.pegasus.kafka.entity.vo.PermissionInfo;
import com.pegasus.kafka.mapper.SysPermissionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The service for table 'sys_permission'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysPermissionService extends ServiceImpl<SysPermissionMapper, SysPermission> {

    @TranRead
    public Map<Long, PageInfo> getPermissionPages(Long sysAdminId) {
        return this.baseMapper.getPermission(sysAdminId);
    }

    @TranRead
    public IPage list(Integer pageNum, Integer pageSize, Long sysRoleId, Long sysPageId) {
        IPage<PermissionInfo> page = new Page<>(pageNum, pageSize);
        List<PermissionInfo> list = this.baseMapper.list(page, sysRoleId, sysPageId);
        page.setRecords(list);
        return page;
    }
}