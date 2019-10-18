package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.vo.TopicVo;
import com.pegasus.kafka.service.kafka.KafkaTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
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

    @RequestMapping("toadd")
    public String toAdd() {
        return "topic/add";
    }

    @RequestMapping("toedit/{topicName}")
    public String toAdd(Model model, @PathVariable(name = "topicName", required = true) String topicName) throws Exception {
        List<TopicVo> topicVos = kafkaTopicService.listTopicNames(topicName, KafkaTopicService.SearchType.EQUALS);
        if (topicVos != null && topicVos.size() > 0) {
            TopicVo topicVo = topicVos.get(0);
            model.addAttribute("topicName", topicName);
            model.addAttribute("partitionNum", topicVo.getPartitionNum());
        }
        return "topic/edit";
    }

    @RequestMapping("list")
    @ResponseBody
    public Result<List<TopicVo>> list(@RequestParam(value = "topicName", required = false) String topicName,
                                      @RequestParam(value = "page", required = true) Integer pageNum,
                                      @RequestParam(value = "limit", required = true) Integer pageSize) throws Exception {
        List<TopicVo> topicInfoList = kafkaTopicService.listTopicNames(topicName, KafkaTopicService.SearchType.LIKE);
        return Result.success(topicInfoList, topicInfoList.size());
    }

    @RequestMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(name = "topicName", required = true) String topicName,
                         @RequestParam(name = "partitionNumber", required = true) Integer partitionNumber,
                         @RequestParam(name = "replicationNumber", required = true) Integer replicationNumber) throws Exception {
        kafkaTopicService.add(topicName, partitionNumber, replicationNumber);
        return Result.success();
    }

    @RequestMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(name = "topicName", required = true) String topicName,
                          @RequestParam(name = "partitionNumber", required = true) Integer partitionNumber) throws Exception {
        kafkaTopicService.edit(topicName, partitionNumber);
        return Result.success();
    }

    @RequestMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "topicName", required = true) String topicName) throws Exception {
        kafkaTopicService.delete(topicName);
        return Result.success();
    }
}
