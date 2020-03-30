package com.pegasus.kafka.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysAlertTopic;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.dto.SysAlertTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.pegasus.kafka.controller.AlertTopicController.PREFIX;


/**
 * The controller for providing the ability of alert for topic.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         27/3/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class AlertTopicController {
    public static final String PREFIX = "alerttopic";
    private final SysAlertTopicService sysAlertTopicService;
    private final KafkaService kafkaService;

    public AlertTopicController(SysAlertTopicService sysAlertTopicService,
                                KafkaService kafkaService) {
        this.sysAlertTopicService = sysAlertTopicService;
        this.kafkaService = kafkaService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("toadd")
    public String toAdd(Model model) throws Exception {
        model.addAttribute("topics", kafkaService.listTopicNames());
        return String.format("%s/add", PREFIX);
    }

    @GetMapping("toedit/{id}")
    public String toEdit(Model model,
                         @PathVariable(required = true, value = "id") String id) throws Exception {
        SysAlertTopic sysAlertTopic = sysAlertTopicService.getById(id);
        model.addAttribute("topics", kafkaService.listTopicNames());
        model.addAttribute("item", sysAlertTopic);
        return String.format("%s/edit", PREFIX);
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<SysAlertTopic>> list(@RequestParam(value = "page", required = true) Integer pageNum,
                                            @RequestParam(value = "limit", required = true) Integer pageSize) {
        QueryWrapper<SysAlertTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(SysAlertTopic::getCreateTime);
        return Result.ok(this.sysAlertTopicService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }


    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "topicName", required = true) String topicName,
                         @RequestParam(value = "fromTime", required = true) String fromTime,
                         @RequestParam(value = "toTime", required = true) String toTime,
                         @RequestParam(value = "fromTps", required = true) Integer fromTps,
                         @RequestParam(value = "toTps", required = true) Integer toTps,
                         @RequestParam(value = "fromMomTps", required = true) Integer fromMomTps,
                         @RequestParam(value = "toMomTps", required = true) Integer toMomTps,
                         @RequestParam(value = "email", required = true) String email
    ) {
        sysAlertTopicService.save(topicName, fromTime, toTime, fromTps, toTps, fromMomTps, toMomTps, email);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id", required = true) Long id,
                          @RequestParam(value = "topicName", required = true) String topicName,
                          @RequestParam(value = "fromTime", required = true) String fromTime,
                          @RequestParam(value = "toTime", required = true) String toTime,
                          @RequestParam(value = "fromTps", required = true) Integer fromTps,
                          @RequestParam(value = "toTps", required = true) Integer toTps,
                          @RequestParam(value = "fromMomTps", required = true) Integer fromMomTps,
                          @RequestParam(value = "toMomTps", required = true) Integer toMomTps,
                          @RequestParam(value = "email", required = true) String email
    ) {
        sysAlertTopicService.update(id, topicName, fromTime, toTime, fromTps, toTps, fromMomTps, toMomTps, email);
        return Result.ok();
    }


    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id", required = true) Long id) {
        sysAlertTopicService.removeById(id);
        return Result.ok();
    }

}