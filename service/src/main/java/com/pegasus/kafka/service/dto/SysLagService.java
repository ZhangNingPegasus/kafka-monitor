package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.mapper.SysLagMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * The service for table 'sys_lag'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysLagService extends ServiceImpl<SysLagMapper, SysLag> {

    @TranRead
    public List<SysLag> listByGroupId(String topicName,
                                      String groupId,
                                      Date from,
                                      Date to) {
        QueryWrapper<SysLag> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<SysLag> lambda = queryWrapper.lambda()
                .eq(SysLag::getTopicName, topicName)
                .eq(SysLag::getConsumerName, groupId)
                .ge(SysLag::getCreateTime, from)
                .le(SysLag::getCreateTime, to)
                .orderByAsc(SysLag::getCreateTime);
        return this.list(queryWrapper);
    }

    @TranSave
    public boolean deleteTopic(String topicName) {
        QueryWrapper<SysLag> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysLag::getTopicName, topicName);
        return this.remove(queryWrapper);
    }

    @TranSave
    public boolean deleteConsumerName(String consumerName) {
        QueryWrapper<SysLag> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysLag::getConsumerName, consumerName);
        return this.remove(queryWrapper);
    }

    @TranRead
    public List<SysLag> listTopLag(int top) {
        return this.baseMapper.listTopLag(top);
    }

    @TranSave
    public void batchSave(List<SysLag> sysLagList) {
        if (sysLagList == null || sysLagList.size() < 1) {
            return;
        }
        this.baseMapper.batchSave(sysLagList);
    }
}
