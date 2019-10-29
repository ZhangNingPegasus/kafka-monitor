package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.entity.dto.SysLogSize;
import com.pegasus.kafka.entity.vo.KafkaConsumerInfo;
import com.pegasus.kafka.entity.vo.OffsetInfo;
import com.pegasus.kafka.mapper.SysLogSizeMapper;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SysLogSizeService extends ServiceImpl<SysLogSizeMapper, SysLogSize> {
    private final KafkaConsumerService kafkaConsumerService;

    public SysLogSizeService(KafkaConsumerService kafkaConsumerService) {
        this.kafkaConsumerService = kafkaConsumerService;
    }

    public Matrix collect() throws Exception {
        Matrix result = new Matrix();
        List<SysLag> sysLagList = new ArrayList<>();
        Map<String, Long> sysLogSizeMap = new HashMap<>();
        List<KafkaConsumerInfo> kafkaConsumerInfos = kafkaConsumerService.listKafkaConsumers();

        for (KafkaConsumerInfo kafkaConsumerInfo : kafkaConsumerInfos) {
            Set<String> topicNames = kafkaConsumerInfo.getTopicNames();
            for (String topicName : topicNames) {
                Long logSize = 0L;
                Long lag = 0L;
                try {
                    List<OffsetInfo> offsetInfos = kafkaConsumerService.listOffsetInfo(kafkaConsumerInfo.getGroupId(), topicName);
                    for (OffsetInfo offsetInfo : offsetInfos) {
                        if (offsetInfo.getLag() != null && offsetInfo.getLag() > 0) {
                            lag += offsetInfo.getLag();
                        }
                        logSize += offsetInfo.getLogSize();
                    }
                    SysLag sysLag = new SysLag();
                    sysLag.setConsumerName(kafkaConsumerInfo.getGroupId());
                    sysLag.setTopicName(topicName);
                    sysLag.setLag(lag);
                    sysLagList.add(sysLag);
                    sysLogSizeMap.put(topicName, logSize);
                } catch (Exception ignored) {
                    continue;
                }
            }
        }

        List<SysLogSize> sysLogSizeList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : sysLogSizeMap.entrySet()) {
            SysLogSize sysLogSize = new SysLogSize();
            sysLogSize.setTopicName(entry.getKey());
            sysLogSize.setLogSize(entry.getValue());
            sysLogSizeList.add(sysLogSize);
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
    public List<SysLogSize> getTopicRank(Integer rank) {
        return this.baseMapper.getTopicRank(rank);
    }

    @Data
    public static class Matrix {
        private List<SysLag> sysLagList;
        private List<SysLogSize> sysLogSizeList;
    }
}
