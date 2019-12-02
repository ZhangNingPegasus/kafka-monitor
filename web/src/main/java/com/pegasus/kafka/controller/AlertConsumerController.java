package com.pegasus.kafka.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysAlertConsumer;
import com.pegasus.kafka.entity.vo.KafkaConsumerInfo;
import com.pegasus.kafka.entity.vo.KafkaTopicInfo;
import com.pegasus.kafka.service.dto.SysAlertConsumerService;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
import com.pegasus.kafka.service.kafka.KafkaTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.pegasus.kafka.controller.AlertConsumerController.PREFIX;

/**
 * The controller for providing the ability of alert for consumer.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class AlertConsumerController {
    public static final String PREFIX = "alertconsumer";
    private final SysAlertConsumerService sysAlertConsumerService;
    private final KafkaConsumerService kafkaConsumerService;
    private final KafkaTopicService kafkaTopicService;


    public AlertConsumerController(SysAlertConsumerService sysAlertConsumerService, KafkaConsumerService kafkaConsumerService, KafkaTopicService kafkaTopicService, KafkaTopicService kafkaTopicService1) {
        this.sysAlertConsumerService = sysAlertConsumerService;
        this.kafkaConsumerService = kafkaConsumerService;
        this.kafkaTopicService = kafkaTopicService1;
    }

    @RequestMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @RequestMapping("toadd")
    public String toAdd(Model model) throws Exception {
        List<KafkaConsumerInfo> kafkaConsumerInfoList = kafkaConsumerService.listKafkaConsumers();
        model.addAttribute("consumers", kafkaConsumerInfoList);
        return String.format("%s/add", PREFIX);
    }

    @RequestMapping("toedit/{id}")
    public String toEdit(Model model,
                         @PathVariable(required = true, value = "id") String id) throws Exception {

        SysAlertConsumer sysAlertConsumer = sysAlertConsumerService.getById(id);
        List<KafkaConsumerInfo> kafkaConsumerInfoList = kafkaConsumerService.listKafkaConsumers();
        model.addAttribute("consumers", kafkaConsumerInfoList);
        model.addAttribute("item", sysAlertConsumer);
        model.addAttribute("topics", listTopics(sysAlertConsumer.getGroupId()).getData());
        return String.format("%s/edit", PREFIX);
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<SysAlertConsumer>> list(@RequestParam(value = "page", required = true) Integer pageNum,
                                               @RequestParam(value = "limit", required = true) Integer pageSize) {
        QueryWrapper<SysAlertConsumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(SysAlertConsumer::getCreateTime);
        return Result.success(this.sysAlertConsumerService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @PostMapping("listTopics")
    @ResponseBody
    public Result<List<String>> listTopics(@RequestParam(value = "groupId", required = true) String groupId) throws Exception {
        List<KafkaTopicInfo> kafkaTopicInfoList = kafkaTopicService.listTopics(false, false, true, false, false);
        List<String> topicNames = kafkaTopicInfoList.stream().filter(p -> Arrays.asList(p.getSubscribeGroupIds()).contains(groupId)).map(KafkaTopicInfo::getTopicName).distinct().collect(Collectors.toList());
        return Result.success(topicNames);
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "groupId", required = true) String groupId,
                         @RequestParam(value = "topicName", required = true) String topicName,
                         @RequestParam(value = "lagThreshold", required = true) Long lagThreshold,
                         @RequestParam(value = "email", required = true) String email
    ) {
        sysAlertConsumerService.save(groupId, topicName, lagThreshold, email);
        return Result.success();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id", required = true) Long id,
                          @RequestParam(value = "groupId", required = true) String groupId,
                          @RequestParam(value = "topicName", required = true) String topicName,
                          @RequestParam(value = "lagThreshold", required = true) Long lagThreshold,
                          @RequestParam(value = "email", required = true) String email
    ) {
        sysAlertConsumerService.update(id, groupId, topicName, lagThreshold, email);
        return Result.success();
    }


    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id", required = true) Long id) {
        sysAlertConsumerService.removeById(id);
        return Result.success();
    }

}
