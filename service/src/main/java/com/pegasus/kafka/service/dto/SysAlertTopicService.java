package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.SysAlertTopic;
import com.pegasus.kafka.mapper.SysAlertTopicMapper;
import org.springframework.stereotype.Service;

/**
 * The service for table 'sys_alert_topic'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysAlertTopicService extends ServiceImpl<SysAlertTopicMapper, SysAlertTopic> {

    @TranSave
    public boolean save(String topicName,
                        String fromTime,
                        String toTime,
                        Integer fromTps,
                        Integer toTps,
                        Integer fromMomTps,
                        Integer toMomTps,
                        String email
    ) {
        SysAlertTopic sysAlertTopic = new SysAlertTopic();
        sysAlertTopic.setTopicName(topicName);
        sysAlertTopic.setFromTime(fromTime);
        sysAlertTopic.setToTime(toTime);
        sysAlertTopic.setFromTps(fromTps);
        sysAlertTopic.setToTps(toTps);
        sysAlertTopic.setFromMomTps(fromMomTps);
        sysAlertTopic.setToMomTps(toMomTps);
        sysAlertTopic.setEmail(email);
        return this.save(sysAlertTopic);
    }

    @TranSave
    public boolean update(Long id,
                          String topicName,
                          String fromTime,
                          String toTime,
                          Integer fromTps,
                          Integer toTps,
                          Integer fromMomTps,
                          Integer toMomTps,
                          String email) {
        UpdateWrapper<SysAlertTopic> updateWrapper = new UpdateWrapper<>();

        updateWrapper.lambda()
                .eq(SysAlertTopic::getId, id)
                .set(SysAlertTopic::getTopicName, topicName)
                .set(SysAlertTopic::getFromTime, fromTime)
                .set(SysAlertTopic::getToTime, toTime)
                .set(SysAlertTopic::getFromTps, fromTps)
                .set(SysAlertTopic::getToTps, toTps)
                .set(SysAlertTopic::getFromMomTps, fromMomTps)
                .set(SysAlertTopic::getToMomTps, toMomTps)
                .set(SysAlertTopic::getEmail, email);

        return this.update(updateWrapper);
    }
}
