package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.entity.dto.SysAlertTopic;
import com.pegasus.kafka.mapper.SysAlertTopicMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        if (StringUtils.isEmpty(topicName)) {
            throw new BusinessException("主题名称不允许为空");
        }

        SysAlertTopic orgiSysAlertTopic = getByTopicName(topicName);
        if (orgiSysAlertTopic != null) {
            throw new BusinessException(String.format("主题[%s]的TPS设置已存在", topicName));
        }

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
        if (StringUtils.isEmpty(topicName)) {
            throw new BusinessException("主题名称不允许为空");
        }

        SysAlertTopic orgiSysAlertTopic = getByTopicName(topicName);
        if (orgiSysAlertTopic != null) {
            if (!topicName.equals(topicName)) {
                throw new BusinessException(String.format("主题[%s]的TPS设置已存在", topicName));
            }
        }

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

    @TranRead
    public List<String> listTopicNames() {
        QueryWrapper<SysAlertTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(SysAlertTopic::getTopicName);
        return this.list(queryWrapper).stream().map(SysAlertTopic::getTopicName).collect(Collectors.toList());
    }

    @TranRead
    public SysAlertTopic getByTopicName(String topicName) {
        if (StringUtils.isEmpty(topicName)) {
            return null;
        }
        QueryWrapper<SysAlertTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAlertTopic::getTopicName, topicName);
        return this.getOne(queryWrapper);
    }
}
