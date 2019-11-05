package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.vo.KafkaTopicInfo;
import com.pegasus.kafka.entity.vo.KafkaTopicPartitionInfo;
import com.pegasus.kafka.entity.vo.MBeanInfo;
import com.pegasus.kafka.service.kafka.KafkaBrokerService;
import com.pegasus.kafka.service.kafka.KafkaTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("topic")
public class TopicController {

    private final KafkaTopicService kafkaTopicService;
    private final KafkaBrokerService kafkaBrokerService;


    public TopicController(KafkaTopicService kafkaTopicService, KafkaBrokerService kafkaBrokerService) {
        this.kafkaTopicService = kafkaTopicService;
        this.kafkaBrokerService = kafkaBrokerService;
    }

    @RequestMapping("tolist")
    public String toList() {
        return "topic/list";
    }

    @RequestMapping("toadd")
    public String toAdd(Model model) {
        int brokerSize = 1;
        try {
            brokerSize = kafkaBrokerService.listAllBrokers().size();
        } catch (Exception ignored) {
        }
        model.addAttribute("brokerSize", brokerSize);
        return "topic/add";
    }

    @RequestMapping("toedit/{topicName}")
    public String toAdd(Model model, @PathVariable(name = "topicName", required = true) String topicName) throws Exception {
        topicName = topicName.trim();
        List<KafkaTopicInfo> topicInfoList = kafkaTopicService.listTopics(topicName, KafkaTopicService.SearchType.EQUALS, false, true, false, false, false);
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

    @PostMapping("list")
    @ResponseBody
    public Result<List<KafkaTopicInfo>> list(@RequestParam(value = "topicName", required = false) String topicName,
                                             @RequestParam(value = "page", required = true) Integer pageNum,
                                             @RequestParam(value = "limit", required = true) Integer pageSize) throws Exception {
        if (!StringUtils.isEmpty(topicName)) {
            topicName = topicName.trim();
        }
        pageNum = Math.min(pageNum, Constants.MAX_PAGE_NUM);
        List<KafkaTopicInfo> topicInfoList = kafkaTopicService.listTopics(topicName, KafkaTopicService.SearchType.LIKE, true, true, true, true, true);
        return Result.success(topicInfoList.stream().skip(pageSize * (pageNum - 1))
                .limit(pageSize).collect(Collectors.toList()), topicInfoList.size());
    }

    @PostMapping("sendmsg")
    @ResponseBody
    public Result<?> sendMsg(@RequestParam(value = "topicName", required = true) String topicName,
                             @RequestParam(value = "content", required = true) String content) throws Exception {
        kafkaTopicService.sendMessage(topicName, content);
        return Result.success();
    }

    @PostMapping("todetail/listTopicSize")
    @ResponseBody
    public Result<String> listTopicSize(@RequestParam(name = "topicName", required = true) String topicName) throws Exception {
        return Result.success(kafkaTopicService.listTopicSize(topicName.trim()));
    }

    @PostMapping("todetail/listTopicMBean/{topicName}")
    @ResponseBody
    public Result<List<MBeanInfo>> listTopicMBean(@PathVariable(name = "topicName", required = true) String topicName) throws Exception {
        return Result.success(kafkaTopicService.listTopicMBean(topicName.trim()));
    }


    @PostMapping("todetail/listTopicDetails/{topicName}")
    @ResponseBody
    public Result<List<KafkaTopicPartitionInfo>> listTopicDetails(@PathVariable(name = "topicName", required = true) String topicName) throws Exception {
        return Result.success(kafkaTopicService.listTopicDetails(topicName.trim()));
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(name = "topicName", required = true) String topicName,
                         @RequestParam(name = "partitionNumber", required = true) Integer partitionNumber,
                         @RequestParam(name = "replicationNumber", required = true) Integer replicationNumber) {
        kafkaTopicService.add(topicName.trim(), partitionNumber, replicationNumber);
        return Result.success();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(name = "topicName", required = true) String topicName,
                          @RequestParam(name = "partitionNumber", required = true) Integer partitionNumber) throws Exception {
        kafkaTopicService.edit(topicName.trim(), partitionNumber);
        return Result.success();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "topicName", required = true) String topicName) throws Exception {
        kafkaTopicService.delete(topicName.trim());
        return Result.success();
    }
}
