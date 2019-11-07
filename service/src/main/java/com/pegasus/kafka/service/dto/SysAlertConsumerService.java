package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.SysAlertConsumer;
import com.pegasus.kafka.mapper.SysAlertConsumerMapper;
import org.springframework.stereotype.Service;

/**
 * The service for table 'sys_alert_consumer'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysAlertConsumerService extends ServiceImpl<SysAlertConsumerMapper, SysAlertConsumer> {

    @TranSave
    public boolean save(String groupId, String topicName, Long lagThreshold, String email) {
        SysAlertConsumer sysAlertConsumer = new SysAlertConsumer();
        sysAlertConsumer.setGroupId(groupId);
        sysAlertConsumer.setTopicName(topicName);
        sysAlertConsumer.setLagThreshold(lagThreshold);
        sysAlertConsumer.setEmail(email);
        return this.save(sysAlertConsumer);
    }

    @TranSave
    public void update(Long id, String groupId, String topicName, Long lagThreshold, String email) {
        UpdateWrapper<SysAlertConsumer> updateWrapper = new UpdateWrapper();
        updateWrapper.lambda().eq(SysAlertConsumer::getId, id)
                .set(SysAlertConsumer::getGroupId, groupId)
                .set(SysAlertConsumer::getTopicName, topicName)
                .set(SysAlertConsumer::getLagThreshold, lagThreshold)
                .set(SysAlertConsumer::getEmail, email);
        this.update(updateWrapper);
    }
}
