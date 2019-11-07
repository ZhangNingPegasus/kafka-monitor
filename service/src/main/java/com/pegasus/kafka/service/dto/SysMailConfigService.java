package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.SysMailConfig;
import com.pegasus.kafka.mapper.SysMailConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service for table 'sys_mail_config'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysMailConfigService extends ServiceImpl<SysMailConfigMapper, SysMailConfig> {


    @TranSave
    public int save(String host, String port, String username, String password) {
        QueryWrapper<SysMailConfig> queryWrapper = new QueryWrapper<>();
        this.baseMapper.delete(queryWrapper);
        SysMailConfig sysMailConfig = new SysMailConfig();
        sysMailConfig.setHost(host);
        sysMailConfig.setPort(port);
        sysMailConfig.setUsername(username);
        sysMailConfig.setPassword(password);
        return this.baseMapper.insert(sysMailConfig);
    }

    @TranRead
    public SysMailConfig get() {
        List<SysMailConfig> list = this.list();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


}