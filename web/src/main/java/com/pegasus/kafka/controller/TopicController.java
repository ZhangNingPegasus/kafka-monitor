package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.vo.KafkaTopicInfo;
import com.pegasus.kafka.entity.vo.KafkaTopicPartitionInfo;
import com.pegasus.kafka.entity.vo.MBeanInfo;
import com.pegasus.kafka.service.kafka.KafkaTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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
        topicName = topicName.trim();
        List<KafkaTopicInfo> topicInfoList = kafkaTopicService.listTopicNames(topicName, KafkaTopicService.SearchType.EQUALS);
        if (topicInfoList != null && topicInfoList.size() > 0) {
            KafkaTopicInfo topicVo = topicInfoList.get(0);
            List<KafkaTopicPartitionInfo> topicDetails = kafkaTopicService.listTopicDetails(topicName);
            for (KafkaTopicPartitionInfo topicDetail : topicDetails) {
                if (topicDetail.getReplicas() != null) {
                    model.addAttribute("replicasNum", topicDetail.getReplicas().size());
                    break;
                }
            }
            model.addAttribute("topicName", topicName);
            model.addAttribute("partitionNum", topicVo.getPartitionNum());
        }
        return "topic/edit";
    }

    @RequestMapping("todetail/{topicName}")
    public String toDetail(Model model, @PathVariable(name = "topicName", required = true) String topicName) {
        topicName = topicName.trim();
        model.addAttribute("topicName", topicName.trim());
        return "topic/detail";
    }

    @RequestMapping("tosendmsg/{topicName}")
    public String toSendMsg(Model model, @PathVariable(name = "topicName", required = true) String topicName) {
        model.addAttribute("topicName", topicName.trim());
        return "topic/sendmsg";
    }

    @RequestMapping("list")
    @ResponseBody
    public Result<List<KafkaTopicInfo>> list(@RequestParam(value = "topicName", required = false) String topicName,
                                             @RequestParam(value = "page", required = true) Integer pageNum,
                                             @RequestParam(value = "limit", required = true) Integer pageSize) throws Exception {
        if (!StringUtils.isEmpty(topicName)) {
            topicName = topicName.trim();
        }
        List<KafkaTopicInfo> topicInfoList = kafkaTopicService.listTopicNames(topicName, KafkaTopicService.SearchType.LIKE);
        return Result.success(topicInfoList, topicInfoList.size());
    }

    @RequestMapping("sendmsg")
    @ResponseBody
    public Result<?> sendMsg(@RequestParam(value = "topicName", required = true) String topicName,
                             @RequestParam(value = "content", required = true) String content) throws Exception {
        kafkaTopicService.sendMessage(topicName, content);
        return Result.success();
    }

    @RequestMapping("todetail/listTopicSize")
    @ResponseBody
    public Result<String> listTopicSize(@RequestParam(name = "topicName", required = true) String topicName) throws Exception {
        return Result.success(kafkaTopicService.listTopicSize(topicName.trim()));
    }

    @RequestMapping("todetail/listTopicMBean/{topicName}")
    @ResponseBody
    public Result<List<MBeanInfo>> listTopicMBean(@PathVariable(name = "topicName", required = true) String topicName) throws Exception {
        return Result.success(kafkaTopicService.listTopicMBean(topicName.trim()));
    }


    @RequestMapping("todetail/listTopicDetails/{topicName}")
    @ResponseBody
    public Result<List<KafkaTopicPartitionInfo>> listTopicDetails(@PathVariable(name = "topicName", required = true) String topicName) throws Exception {
        return Result.success(kafkaTopicService.listTopicDetails(topicName.trim()));
    }

    @RequestMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(name = "topicName", required = true) String topicName,
                         @RequestParam(name = "partitionNumber", required = true) Integer partitionNumber,
                         @RequestParam(name = "replicationNumber", required = true) Integer replicationNumber) {
        kafkaTopicService.add(topicName.trim(), partitionNumber, replicationNumber);
        return Result.success();
    }

    @RequestMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(name = "topicName", required = true) String topicName,
                          @RequestParam(name = "partitionNumber", required = true) Integer partitionNumber) throws Exception {
        kafkaTopicService.edit(topicName.trim(), partitionNumber);
        return Result.success();
    }

    @RequestMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "topicName", required = true) String topicName) throws Exception {
        kafkaTopicService.delete(topicName.trim());
        return Result.success();
    }
}
