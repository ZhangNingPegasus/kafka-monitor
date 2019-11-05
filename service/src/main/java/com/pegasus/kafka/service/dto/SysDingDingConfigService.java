package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.common.ehcache.EhcacheService;
import com.pegasus.kafka.entity.dto.SysDingDingConfig;
import com.pegasus.kafka.mapper.SysDingDingConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDingDingConfigService extends ServiceImpl<SysDingDingConfigMapper, SysDingDingConfig> {
    private final EhcacheService ehcacheService;

    public SysDingDingConfigService(EhcacheService ehcacheService) {
        this.ehcacheService = ehcacheService;
    }

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
        List<SysDingDingConfig> list = this.list();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
