package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.vo.TopicVo;
import com.pegasus.kafka.service.kafka.KafkaTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("topic")
public class TopicController {

    private final KafkaTopicService kafkaTopicService;

    public TopicController(KafkaTopicService kafkaTopicService) {
        this.kafkaTopicService = kafkaTopicService;
    }

    @RequestMapping("tolist")
    public String toList() {
        return "topic/list";
    }

    @RequestMapping("list")
    @ResponseBody
    public Result<List<TopicVo>> list(@RequestParam(value = "topicName", required = false) String topicName,
                                      @RequestParam(value = "page", required = true) Integer pageNum,
                                      @RequestParam(value = "limit", required = true) Integer pageSize) throws Exception {
        List<TopicVo> topicInfoList = kafkaTopicService.getAllTopics();
        return Result.success(topicInfoList, topicInfoList.size());
    }

    @RequestMapping("exit")
    @ResponseBody
    public void exit() {
        System.exit(0);
    }

}
