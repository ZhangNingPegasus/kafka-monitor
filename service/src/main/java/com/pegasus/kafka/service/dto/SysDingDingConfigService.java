package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.SysDingDingConfig;
import com.pegasus.kafka.mapper.SysDingDingConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service for table 'sys_dingding_config'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysDingDingConfigService extends ServiceImpl<SysDingDingConfigMapper, SysDingDingConfig> {

    @TranSave
    public int save(String accesstoken, String secret) {
        QueryWrapper<SysDingDingConfig> queryWrapper = new QueryWrapper<>();
        this.baseMapper.delete(queryWrapper);
        SysDingDingConfig sysDingDingConfig = new SysDingDingConfig();
        sysDingDingConfig.setAccessToken(accesstoken);
        sysDingDingConfig.setSecret(secret);
        return this.baseMapper.insert(sysDingDingConfig);
    }

    @TranRead
    public SysDingDingConfig get() {
        QueryWrapper<SysDingDingConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.last("LIMIT 1");
        List<SysDingDingConfig> list = this.list(queryWrapper);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}