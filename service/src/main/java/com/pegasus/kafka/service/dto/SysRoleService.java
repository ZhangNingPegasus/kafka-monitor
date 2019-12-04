package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.entity.dto.SysRole;
import com.pegasus.kafka.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * The service for table 'sys_role'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    @TranRead
    public IPage<SysRole> list(Integer pageNum, Integer pageSize, String name) {
        if (!StringUtils.isEmpty(name)) {
            name = name.trim();
        }
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();

        LambdaQueryWrapper<SysRole> lambda = queryWrapper.lambda();
        if (!StringUtils.isEmpty(name)) {
            lambda.like(SysRole::getName, name);
        }
        lambda.orderByAsc(SysRole::getName);
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @TranRead
    public SysRole getByUsername(String name) {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysRole::getName, name);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @TranSave
    public int add(String name, Boolean superAdmin, String remark) {
        SysRole sysRole = this.getByUsername(name);
        if (sysRole == null) {
            sysRole = new SysRole();
            sysRole.setName(name);
            sysRole.setSuperAdmin(superAdmin);
            sysRole.setRemark(remark);
            return this.baseMapper.insert(sysRole);
        }
        throw new BusinessException(String.format("角色名%s已存在", name));
    }

    @TranSave
    public boolean edit(Long id, String name, Boolean superAdmin, String remark) {
        UpdateWrapper<SysRole> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(SysRole::getId, id)
                .set(SysRole::getName, name)
                .set(SysRole::getSuperAdmin, superAdmin)
                .set(SysRole::getRemark, remark);
        return this.update(updateWrapper);
    }

    @TranRead
    public List<SysRole> listOrderByName() {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(SysRole::getName);
        return this.list(queryWrapper);
    }
}
