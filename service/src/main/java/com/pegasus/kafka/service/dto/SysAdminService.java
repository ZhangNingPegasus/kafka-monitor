package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysAdmin;
import com.pegasus.kafka.mapper.SysAdminMapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

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

    @TranRead
    public SysAdmin getByUsername(String username) {
        QueryWrapper<SysAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAdmin::getUsername, username);
        return this.baseMapper.selectOne(queryWrapper);
    }


    public boolean changePassword(SysAdmin sysAdmin, String oldPassword, String newPassword) {
        if (sysAdmin == null) {
            return false;
        }
        if (!sysAdmin.getPassword().equals(Common.hash(oldPassword))) {
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
    public boolean updateInfo(SysAdmin sysAdmin, String name, Boolean gender, String phoneNumber, String email, String remark) {
        if (sysAdmin == null) {
            return false;
        }
        sysAdmin.setName(name);
        sysAdmin.setGender(gender);
        sysAdmin.setPhoneNumber(phoneNumber);
        sysAdmin.setEmail(email);
        sysAdmin.setRemark(remark);
        this.baseMapper.updateById(sysAdmin);

        SysAdmin currentSysAdmin = (SysAdmin) SecurityUtils.getSubject().getPrincipal();
        currentSysAdmin.setName(name);
        currentSysAdmin.setGender(gender);
        currentSysAdmin.setPhoneNumber(phoneNumber);
        currentSysAdmin.setEmail(email);
        currentSysAdmin.setRemark(remark);
        return true;
    }
}
