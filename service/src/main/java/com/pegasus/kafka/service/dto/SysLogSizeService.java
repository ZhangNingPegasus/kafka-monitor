package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.ehcache.EhcacheService;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.entity.dto.SysLogSize;
import com.pegasus.kafka.entity.vo.KafkaConsumerVo;
import com.pegasus.kafka.entity.vo.OffsetVo;
import com.pegasus.kafka.entity.vo.TopicRecordCountVo;
import com.pegasus.kafka.mapper.SysLogSizeMapper;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
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
    private final static int BATCH_SIZE = 1024;
    private final KafkaConsumerService kafkaConsumerService;
    private final EhcacheService ehcacheService;
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;

    public SysLogSizeService(KafkaConsumerService kafkaConsumerService, EhcacheService ehcacheService, KafkaService kafkaService, TopicRecordService topicRecordService) {
        this.kafkaConsumerService = kafkaConsumerService;
        this.ehcacheService = ehcacheService;
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
    }

    private Map<String, SysLogSize> collectLogSize(Date now) throws Exception {
        Map<String, SysLogSize> result = new HashMap<>();
        List<String> topicNames = kafkaService.listTopicNames();
        for (String topicName : topicNames) {
            try {
                SysLogSize sysLogSize = new SysLogSize(topicName, topicRecordService.getRecordsCount(topicName));
                sysLogSize.setCreateTime(now);
                result.put(topicName, sysLogSize);
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    public Matrix kpi(Date now) throws Exception {
        Matrix result = new Matrix();

        Map<String, SysLogSize> logSizeMap = collectLogSize(now);


        List<SysLag> sysLagList = new ArrayList<>(BATCH_SIZE);
        Map<String, Long> sysLogSizeMap = new HashMap<>(BATCH_SIZE);
        List<KafkaConsumerVo> kafkaConsumerVoList = kafkaConsumerService.listKafkaConsumers();
        for (KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
            Set<String> topicNames = kafkaConsumerVo.getTopicNames();
            for (String topicName : topicNames) {
                if (Constants.KAFKA_SYSTEM_TOPIC.contains(topicName)) {
                    continue;
                }
                Long logSize = 0L;
                long lag = 0L;
                long offset = 0L;
                try {
                    List<OffsetVo> offsetVoList = kafkaService.listOffsetVo(kafkaConsumerVoList, kafkaConsumerVo.getGroupId(), topicName);
                    for (OffsetVo offsetVo : offsetVoList) {
                        if (offsetVo.getLag() != null && offsetVo.getLag() >= 0) {
                            lag += offsetVo.getLag();
                        }
                        if (offsetVo.getOffset() != null && offsetVo.getOffset() >= 0) {
                            offset += offsetVo.getOffset();
                        }
                        logSize += offsetVo.getLogSize();
                    }
                    SysLag sysLag = new SysLag();
                    sysLag.setConsumerName(kafkaConsumerVo.getGroupId());
                    sysLag.setTopicName(topicName);
                    sysLag.setOffset(offset);
                    sysLag.setLag(lag);
                    sysLag.setCreateTime(now);
                    sysLagList.add(sysLag);
                    sysLogSizeMap.put(topicName, logSize);
                } catch (Exception ignored) {
                }
            }
        }

        result.setSysLagList(sysLagList);
        result.setSysLogSizeList(new ArrayList<>(logSizeMap.values()));
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

    public Long getHistoryLogSize(String topicName, int days) {
        if (days < 0) {
            throw new BusinessException("天数必须大于等于0");
        }

        String key = String.format("SysLogSizeService::getHistoryLogSize:%s:%s", topicName, days);

        Long result = ehcacheService.get(key);
        if (result == null) {

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
            result = getHistoryLogSize(topicName, from, to);
            if (days != 0) {
                ehcacheService.set(key, result, Common.getSecondsNextEarlyMorning().intValue());
            }
        }
        return result;
    }

    @TranRead
    public Long getHistoryLogSize(String topicName, Date from, Date to) {
        try {
            return this.baseMapper.getHistoryLogSizeFromTable(topicRecordService.convertToTableName(topicName), from, to);
        } catch (Exception e) {
            return 0L;
        }
    }

    @TranRead
    public Long getTotalRecordCount() {
        return this.baseMapper.getTotalRecordCount();
    }

    @TranSave
    public void batchSave(List<SysLogSize> sysLogSizeList) {
        if (sysLogSizeList == null || sysLogSizeList.size() < 1) {
            return;
        }
        this.baseMapper.batchSave(sysLogSizeList);
    }

    public List<TopicRecordCountVo> listTotalRecordCount(int top) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                0,
                0,
                0);
        Date from0 = calendar.getTime();
        Date to0 = DateUtils.addDays(from0, 1);

        Date from1 = DateUtils.addDays(from0, -1);
        Date to1 = DateUtils.addDays(from1, 1);

        return this.baseMapper.listTotalRecordCount(top, from0, to0, from1, to1);
    }

    @Data
    public static class Matrix {
        private List<SysLag> sysLagList;
        private List<SysLogSize> sysLogSizeList;
    }
}
