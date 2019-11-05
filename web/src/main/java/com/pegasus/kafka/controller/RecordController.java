package com.pegasus.kafka.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.entity.vo.*;
import com.pegasus.kafka.service.dto.TopicRecordService;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
import com.pegasus.kafka.service.kafka.KafkaTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("record")
public class RecordController {
    private final TopicRecordService topicRecordService;
    private final KafkaTopicService kafkaTopicService;
    private final KafkaConsumerService kafkaConsumerService;

    public RecordController(KafkaTopicService kafkaTopicService, TopicRecordService topicRecordService, KafkaConsumerService kafkaConsumerService) {
        this.kafkaTopicService = kafkaTopicService;
        this.topicRecordService = topicRecordService;
        this.kafkaConsumerService = kafkaConsumerService;
    }

    @RequestMapping("tolist")
    public String toList(Model model) throws Exception {
        List<KafkaTopicInfo> kafkaTopicInfoList = kafkaTopicService.listTopics(false, false, false, false, false);
        model.addAttribute("topics", kafkaTopicInfoList);
        return "record/list";
    }

    @RequestMapping("todetail")
    public String toDetail(Model model,
                           @RequestParam(value = "topicName", required = true) String topicName,
                           @RequestParam(value = "partitionId", required = true) Integer partitionId,
                           @RequestParam(value = "offset", required = true) Long offset,
                           @RequestParam(value = "key", required = true) String key) {
        TopicRecord topicRecord = topicRecordService.findMessage(topicName, partitionId, offset, key);
        model.addAttribute("record", topicRecord.toVo());
        return "record/detail";
    }

    @PostMapping("listTopicPartitions")
    @ResponseBody
    public Result<List<KafkaTopicPartitionInfo>> listTopicPartitions(@RequestParam(name = "topicName", required = true) String topicName) throws Exception {
        return Result.success(kafkaTopicService.listTopicDetails(topicName));
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<KafkaTopicRecordInfo>> list(@RequestParam(name = "topicName", required = false, defaultValue = "") String topicName,
                                                   @RequestParam(name = "partitionId", required = false) Integer partitionId,
                                                   @RequestParam(name = "key", required = false, defaultValue = "") String key,
                                                   @RequestParam(name = "createTimeRange", required = false, defaultValue = "") String createTimeRange,
                                                   @RequestParam(value = "page", required = true) Integer pageNum,
                                                   @RequestParam(value = "limit", required = true) Integer pageSize) throws Exception {
        topicName = topicName.trim();
        key = key.trim();
        createTimeRange = createTimeRange.trim();
        pageNum = Math.min(pageNum, Constants.MAX_PAGE_NUM);
        if (StringUtils.isEmpty(topicName)) {
            return Result.success();
        }

        IPage page = new Page(pageNum, pageSize);

        Common.TimeRange timeRange = Common.splitTime(createTimeRange);
        Date from = timeRange.getStart(), to = timeRange.getEnd();
        return Result.success(kafkaTopicService.listMessages(page, topicName, partitionId, key, from, to), page.getTotal());
    }

    @PostMapping("resend")
    @ResponseBody
    public Result<?> resend(@RequestParam(name = "topicName", required = true, defaultValue = "") String topicName,
                            @RequestParam(name = "key", required = true, defaultValue = "") String key,
                            @RequestParam(name = "value", required = true, defaultValue = "") String value) throws Exception {
        topicName = topicName.trim();
        key = key.trim();
        value = value.trim();
        if (StringUtils.isEmpty(topicName)) {
            return Result.error("主题不能为空");
        }
        kafkaTopicService.sendMessage(topicName, key, value);
        return Result.success();
    }

    @PostMapping("listTopicConsumers")
    @ResponseBody
    public Result<List<KafkaRecordConsumeInfo>> listTopicConsumers(@RequestParam(name = "topicName", required = true, defaultValue = "") String topicName,
                                                                   @RequestParam(name = "partitionId", required = false) Integer partitionId,
                                                                   @RequestParam(name = "offset", required = false, defaultValue = "") Long offset) throws Exception {

        List<KafkaConsumerInfo> allConsumers = kafkaConsumerService.listKafkaConsumers();
        List<KafkaConsumerInfo> kafkaConsumerInfoList = allConsumers.stream().filter(p -> p.getTopicNames().contains(topicName)).collect(Collectors.toList());

        List<KafkaRecordConsumeInfo> result = new ArrayList<>(kafkaConsumerInfoList.size());

        for (KafkaConsumerInfo kafkaConsumerInfo : kafkaConsumerInfoList) {
            List<OffsetInfo> offsetInfos = kafkaConsumerService.listOffsetInfo(kafkaConsumerInfo.getGroupId(), topicName);
            Optional<OffsetInfo> first = offsetInfos.stream().filter(p -> p.getPartitionId().equals(partitionId)).findFirst();
            if (first.isPresent()) {
                OffsetInfo offsetInfo = first.get();
                KafkaRecordConsumeInfo recordConsumeInfo = new KafkaRecordConsumeInfo();
                recordConsumeInfo.setGroupId(kafkaConsumerInfo.getGroupId());
                recordConsumeInfo.setIsConsume((offsetInfo.getOffset() >= offset) ? 1 : 0);
                result.add(recordConsumeInfo);
            }
        }
        return Result.success(result);
    }
}