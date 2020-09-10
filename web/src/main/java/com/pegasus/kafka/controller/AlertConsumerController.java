package com.pegasus.kafka.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysAlertConsumer;
import com.pegasus.kafka.entity.vo.KafkaConsumerVo;
import com.pegasus.kafka.service.dto.SysAlertConsumerService;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    public AlertConsumerController(SysAlertConsumerService sysAlertConsumerService,
                                   KafkaConsumerService kafkaConsumerService) {
        this.sysAlertConsumerService = sysAlertConsumerService;
        this.kafkaConsumerService = kafkaConsumerService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("toadd")
    public String toAdd(Model model) throws Exception {
        List<KafkaConsumerVo> kafkaConsumerVoList = kafkaConsumerService.listKafkaConsumers();
        model.addAttribute("consumers", kafkaConsumerVoList);
        return String.format("%s/add", PREFIX);
    }

    @GetMapping("toedit")
    public String toEdit(Model model,
                         @RequestParam(required = true, value = "id") String id) throws Exception {
        SysAlertConsumer sysAlertConsumer = sysAlertConsumerService.getById(id);
        List<KafkaConsumerVo> kafkaConsumerVoList = kafkaConsumerService.listKafkaConsumers();
        model.addAttribute("consumers", kafkaConsumerVoList);
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
        return Result.ok(this.sysAlertConsumerService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @PostMapping("listTopics")
    @ResponseBody
    public Result<List<String>> listTopics(@RequestParam(value = "groupId", required = true) String groupId) throws Exception {
        List<String> result = new ArrayList<>();
        List<KafkaConsumerVo> kafkaConsumerVoList = kafkaConsumerService.listKafkaConsumersByGroupdId(groupId);
        for (KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
            result.addAll(kafkaConsumerVo.getTopicNames());
        }
        return Result.ok(result);
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "groupId", required = true) String groupId,
                         @RequestParam(value = "topicName", required = true) String topicName,
                         @RequestParam(value = "lagThreshold", required = true) Long lagThreshold,
                         @RequestParam(value = "email", required = true) String email
    ) {
        sysAlertConsumerService.save(groupId, topicName, lagThreshold, email);
        return Result.ok();
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
        return Result.ok();
    }


    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id", required = true) Long id) {
        sysAlertConsumerService.removeById(id);
        return Result.ok();
    }

}
