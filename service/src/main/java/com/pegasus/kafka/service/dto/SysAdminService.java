package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysAdmin;
import com.pegasus.kafka.entity.dto.SysRole;
import com.pegasus.kafka.entity.vo.AdminVo;
import com.pegasus.kafka.mapper.SysAdminMapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * The service for table 'sys_admin'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysAdminService extends ServiceImpl<SysAdminMapper, SysAdmin> {

    private final SysRoleService sysRoleService;

    public SysAdminService(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @TranRead
    public IPage<AdminVo> list(Integer pageNum, Integer pageSize, String name) {
        if (!StringUtils.isEmpty(name)) {
            name = name.trim();
        }
        return this.baseMapper.list(new Page<>(pageNum, pageSize), name);
    }

    @TranRead
    public SysAdmin getByUsername(String username) {
        QueryWrapper<SysAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAdmin::getUsername, username);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @TranRead
    public AdminVo getByUsernameAndPassword(String username, String password) {
        AdminVo adminVo = this.baseMapper.getByUsernameAndPassword(username, password);
        if (adminVo == null) {
            return null;
        }
        SysRole sysRole = sysRoleService.getById(adminVo.getSysRoleId());
        adminVo.setSysRole(sysRole);
        return adminVo;
    }


    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        if (id == null) {
            return false;
        }

        SysAdmin sysAdmin = this.getById(id);

        if (sysAdmin == null || !sysAdmin.getPassword().equals(Common.hash(oldPassword))) {
            return false;
        }
        changePwd(sysAdmin, newPassword);
        return true;
    }

    @TranSave
    void changePwd(SysAdmin sysAdmin, String newPassword) {
        sysAdmin.setPassword(Common.hash(newPassword));
        this.baseMapper.updateById(sysAdmin);
    }

    @TranSave
    public boolean resetPassword(Long id) {
        UpdateWrapper<SysAdmin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(SysAdmin::getId, id)
                .set(SysAdmin::getPassword, Constants.DEFAULT_ADMIN_PASSWORD);
        return this.update(updateWrapper);
    }

    @TranSave
    public boolean updateInfo(Long id, String name, Boolean gender, String phoneNumber, String email, String remark) {
        if (id == null) {
            return false;
        }

        SysAdmin sysAdmin = getById(id);

        if (sysAdmin == null) {
            return false;
        }
        sysAdmin.setName(name);
        sysAdmin.setGender(gender);
        sysAdmin.setPhoneNumber(phoneNumber);
        sysAdmin.setEmail(email);
        sysAdmin.setRemark(remark);
        this.baseMapper.updateById(sysAdmin);

        AdminVo currentAdminVo = (AdminVo) SecurityUtils.getSubject().getPrincipal();
        currentAdminVo.setName(name);
        currentAdminVo.setGender(gender);
        currentAdminVo.setPhoneNumber(phoneNumber);
        currentAdminVo.setEmail(email);
        currentAdminVo.setRemark(remark);
        return true;
    }

    @TranSave
    public int add(Long roleId, String username, String password, String name, Boolean gender, String phoneNumber, String email, String remark) {
        SysAdmin sysAdmin = this.getByUsername(username);
        if (sysAdmin == null) {
            sysAdmin = new SysAdmin();
            sysAdmin.setSysRoleId(roleId);
            sysAdmin.setUsername(username);
            sysAdmin.setPassword(Common.hash(password));
            sysAdmin.setName(name);
            sysAdmin.setGender(gender);
            sysAdmin.setPhoneNumber(phoneNumber);
            sysAdmin.setEmail(email);
            sysAdmin.setRemark(remark);
            return this.baseMapper.insert(sysAdmin);
        }
        throw new BusinessException(String.format("用户名%s已存在", username));
    }

    @TranSave
    public boolean edit(Long id, Long roleId, String username, String name, Boolean gender, String phoneNumber, String email, String remark) {
        UpdateWrapper<SysAdmin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(SysAdmin::getId, id)
                .set(SysAdmin::getSysRoleId, roleId)
                .set(SysAdmin::getName, name)
                .set(SysAdmin::getGender, gender)
                .set(SysAdmin::getPhoneNumber, phoneNumber)
                .set(SysAdmin::getEmail, email)
                .set(SysAdmin::getRemark, remark);
        return this.update(updateWrapper);
    }

    @TranRead
    public List<SysAdmin> getByRoleId(Long roleId) {
        QueryWrapper<SysAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAdmin::getSysRoleId, roleId);
        return this.list(queryWrapper);
    }
}
