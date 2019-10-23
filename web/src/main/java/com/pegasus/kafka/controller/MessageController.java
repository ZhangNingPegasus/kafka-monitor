package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.vo.KafkaMessageInfo;
import com.pegasus.kafka.entity.vo.KafkaTopicInfo;
import com.pegasus.kafka.entity.vo.KafkaTopicPartitionInfo;
import com.pegasus.kafka.service.kafka.KafkaTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("message")
public class MessageController {

    private final KafkaTopicService kafkaTopicService;

    public MessageController(KafkaTopicService kafkaTopicService) {
        this.kafkaTopicService = kafkaTopicService;
    }

    @RequestMapping("tolist")
    public String toList(Model model) throws Exception {
        List<KafkaTopicInfo> kafkaTopicInfoList = kafkaTopicService.listTopicNames();
        model.addAttribute("topics", kafkaTopicInfoList);
        return "message/list";
    }

    @RequestMapping("listTopicPartitions")
    @ResponseBody
    public Result<List<KafkaTopicPartitionInfo>> listTopicPartitions(@RequestParam(name = "topicName", required = true) String topicName) throws Exception {
        return Result.success(kafkaTopicService.listTopicDetails(topicName));
    }

    @RequestMapping("list")
    @ResponseBody
    public Result<List<KafkaMessageInfo>> list(@RequestParam(name = "", required = false) String topicName,
                                               @RequestParam(name = "", required = false) Integer partitionNum,
                                               @RequestParam(value = "page", required = true) Integer pageNum,
                                               @RequestParam(value = "limit", required = true) Integer pageSize) throws Exception {
        if (StringUtils.isEmpty(topicName) || partitionNum == null) {
            return Result.success();
        }
        topicName = topicName.trim();
        List<Integer> partitionNumList = new ArrayList<>();
        if (partitionNum < 0) {
            List<KafkaTopicPartitionInfo> topicDetails = kafkaTopicService.listTopicDetails(topicName);
            partitionNumList.addAll(topicDetails.stream().map(p -> Integer.parseInt(p.getPartitionId())).collect(Collectors.toList()));
        } else {
            partitionNumList.add(partitionNum);
        }

        return Result.success(kafkaTopicService.listMessages(topicName, partitionNumList.toArray(new Integer[]{}), pageNum, pageSize), kafkaTopicService.getLogsize(topicName));
    }
}