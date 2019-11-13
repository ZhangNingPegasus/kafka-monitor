package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.entity.dto.SysLogSize;
import com.pegasus.kafka.entity.vo.KafkaConsumerInfo;
import com.pegasus.kafka.entity.vo.KafkaTopicInfo;
import com.pegasus.kafka.entity.vo.OffsetInfo;
import com.pegasus.kafka.mapper.SysLogSizeMapper;
import com.pegasus.kafka.service.alert.AlertService;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
import com.pegasus.kafka.service.kafka.KafkaRecordService;
import com.pegasus.kafka.service.kafka.KafkaTopicService;
import lombok.Data;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.util.*;

/**
 * The service for table 'sys_log_size'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysLogSizeService extends ServiceImpl<SysLogSizeMapper, SysLogSize> {
    private final KafkaConsumerService kafkaConsumerService;
    private final KafkaTopicService kafkaTopicService;
    private final AlertService alertService;

    public SysLogSizeService(KafkaConsumerService kafkaConsumerService, KafkaTopicService kafkaTopicService, AlertService alertService) {
        this.kafkaConsumerService = kafkaConsumerService;
        this.kafkaTopicService = kafkaTopicService;
        this.alertService = alertService;
    }

    public Matrix kpi(Date now) throws Exception {
        Matrix result = new Matrix();
        List<SysLag> sysLagList = new ArrayList<>(KafkaRecordService.BATCH_SIZE);
        Map<String, Long> sysLogSizeMap = new HashMap<>(KafkaRecordService.BATCH_SIZE);
        List<KafkaConsumerInfo> kafkaConsumerInfos = kafkaConsumerService.listKafkaConsumers();
        for (KafkaConsumerInfo kafkaConsumerInfo : kafkaConsumerInfos) {
            Set<String> topicNames = kafkaConsumerInfo.getTopicNames();
            for (String topicName : topicNames) {
                Long logSize = 0L;
                Long lag = 0L;
                try {
                    List<OffsetInfo> offsetInfos = kafkaConsumerService.listOffsetInfo(kafkaConsumerInfo.getGroupId(), topicName);
                    for (OffsetInfo offsetInfo : offsetInfos) {
                        if (offsetInfo.getLag() != null && offsetInfo.getLag() >= 0) {
                            lag += offsetInfo.getLag();
                        } else {
                            alertService.offer(String.format("订阅组[%s]订阅的主题[%s]有部分分区不可用,请检查.", offsetInfo.getConsumerId(), topicName));
                        }
                        logSize += offsetInfo.getLogSize();
                    }
                    SysLag sysLag = new SysLag();
                    sysLag.setConsumerName(kafkaConsumerInfo.getGroupId());
                    sysLag.setTopicName(topicName);
                    sysLag.setLag(lag);
                    sysLag.setCreateTime(now);
                    sysLagList.add(sysLag);
                    sysLogSizeMap.put(topicName, logSize);
                } catch (Exception ignored) {
                }
            }
        }

        List<SysLogSize> sysLogSizeList = new ArrayList<>(KafkaRecordService.BATCH_SIZE);
        if (sysLogSizeMap.size() > 0) {
            for (Map.Entry<String, Long> entry : sysLogSizeMap.entrySet()) {
                SysLogSize sysLogSize = new SysLogSize();
                sysLogSize.setTopicName(entry.getKey());
                sysLogSize.setLogSize(entry.getValue());
                sysLogSize.setCreateTime(now);
                sysLogSizeList.add(sysLogSize);
            }
        } else {
            List<KafkaTopicInfo> kafkaTopicInfoList = kafkaTopicService.listTopics(false, false, false, true, false);
            for (KafkaTopicInfo kafkaTopicInfo : kafkaTopicInfoList) {
                SysLogSize sysLogSize = new SysLogSize();
                sysLogSize.setTopicName(kafkaTopicInfo.getTopicName());
                sysLogSize.setLogSize(kafkaTopicInfo.getLogSize());
                sysLogSize.setCreateTime(now);
                sysLogSizeList.add(sysLogSize);
            }
        }
        result.setSysLagList(sysLagList);
        result.setSysLogSizeList(sysLogSizeList);
        return result;
    }

    @TranRead
    public List<SysLogSize> listByTopicName(String topicName, Date from, Date to) {
        QueryWrapper<SysLogSize> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<SysLogSize> lambda = queryWrapper.lambda();
        if (!StringUtils.isEmpty(topicName)) {
            lambda.eq(SysLogSize::getTopicName, topicName);
        }
        lambda.ge(SysLogSize::getCreateTime, from)
                .le(SysLogSize::getCreateTime, to)
                .orderByAsc(SysLogSize::getCreateTime);
        return this.list(queryWrapper);
    }

    @TranRead
    public List<SysLogSize> getTopicRank(Integer rank, @Nullable Date from, @Nullable Date to) {
        return this.baseMapper.getTopicRank(rank, from, to);
    }

    @TranSave
    public boolean deleteTopic(String topicName) {
        QueryWrapper<SysLogSize> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysLogSize::getTopicName, topicName);
        return this.remove(queryWrapper);
    }

    @TranRead
    public Long getHistoryLogSize(String topicName, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                0,
                0,
                0);
        Date now = calendar.getTime();

        Date from = DateUtils.addDays(now, -days);
        Date to = DateUtils.addDays(from, 1);

        Long result = this.baseMapper.getHistoryLogSize(topicName, from, to);
        return result == null ? 0L : result;
    }

    public Set<String> listTopicNames() {
        return this.baseMapper.listTopicNames();
    }

    @Data
    public static class Matrix {
        private List<SysLag> sysLagList;
        private List<SysLogSize> sysLogSizeList;
    }
}
