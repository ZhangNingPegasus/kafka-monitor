package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.mapper.SysLagMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SysLagService extends ServiceImpl<SysLagMapper, SysLag> {

    @TranRead
    public List<SysLag> listByGroupId(String groupId, Date from, Date to) {
        QueryWrapper<SysLag> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysLag::getConsumerName, groupId)
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
}
