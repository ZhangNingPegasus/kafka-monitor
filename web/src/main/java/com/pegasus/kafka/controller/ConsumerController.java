package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.echarts.TreeInfo;
import com.pegasus.kafka.entity.vo.KafkaConsumerVo;
import com.pegasus.kafka.entity.vo.KafkaTopicVo;
import com.pegasus.kafka.entity.vo.OffsetVo;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.pegasus.kafka.controller.ConsumerController.PREFIX;

/**
 * The controller for providing the ability of consumer.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class ConsumerController {
    public static final String PREFIX = "consumer";
    private final KafkaConsumerService kafkaConsumerService;

    public ConsumerController(KafkaConsumerService kafkaConsumerService) {
        this.kafkaConsumerService = kafkaConsumerService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("todetail")
    public String toDetail(Model model,
                           @RequestParam(name = "groupId", required = true) String groupId) {
        groupId = groupId.trim();
        model.addAttribute("groupId", groupId);
        return String.format("%s/detail", PREFIX);
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<KafkaConsumerVo>> list(HttpSession httpSession,
                                              @RequestParam(value = "groupId", required = false) String searchGroupId,
                                              @RequestParam(value = "page", required = true) Integer pageNum,
                                              @RequestParam(value = "limit", required = true) Integer pageSize) {
        try {
            List<KafkaConsumerVo> kafkaConsumerVoList = kafkaConsumerService.listKafkaConsumers();

            if (!StringUtils.isEmpty(searchGroupId)) {
                kafkaConsumerVoList = kafkaConsumerVoList.stream()
                        .filter(p -> p.getGroupId().contains(searchGroupId))
                        .collect(Collectors.toList());
            }

            List<KafkaConsumerVo> currentPage = kafkaConsumerVoList.stream()
                    .skip(pageSize * (pageNum - 1))
                    .limit(pageSize)
                    .sorted(Comparator.comparing(KafkaConsumerVo::getGroupId))
                    .collect(Collectors.toList());

            httpSession.setAttribute(Constants.SESSION_KAFKA_CONSUMER_INFO, currentPage);
            return Result.ok(currentPage, kafkaConsumerVoList.size());
        } catch (Exception e) {
            return Result.ok();
        }
    }

    @PostMapping("getChartData")
    @ResponseBody
    public Result<TreeInfo> getChartData(HttpSession httpSession) {
        TreeInfo root = new TreeInfo("消费者 - 主题");
        root.setStyle(TreeInfo.Style.info());
        List<KafkaConsumerVo> kafkaConsumerVoList = (List<KafkaConsumerVo>) httpSession.getAttribute(Constants.SESSION_KAFKA_CONSUMER_INFO);
        httpSession.removeAttribute(Constants.SESSION_KAFKA_CONSUMER_INFO);
        if (kafkaConsumerVoList != null) {
            List<TreeInfo> consuerGroupTreeInfoList = new ArrayList<>(kafkaConsumerVoList.size());

            for (KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
                TreeInfo consumerGroup = new TreeInfo(kafkaConsumerVo.getGroupId());

                if (kafkaConsumerVo.getNotActiveTopicNames().size() == kafkaConsumerVo.getTopicNames().size()) {
                    consumerGroup.setStyle(TreeInfo.Style.warn());
                } else if (kafkaConsumerVo.getActiveTopicNames().size() == kafkaConsumerVo.getTopicNames().size()) {
                    consumerGroup.setStyle(TreeInfo.Style.success());
                } else {
                    consumerGroup.setStyle(TreeInfo.Style.info());
                }

                List<TreeInfo> topicTreeInfoList = new ArrayList<>(kafkaConsumerVo.getTopicCount());
                for (String activeTopicName : kafkaConsumerVo.getActiveTopicNames()) {
                    TreeInfo topicInfo = new TreeInfo(activeTopicName);
                    topicInfo.setStyle(TreeInfo.Style.success());
                    topicTreeInfoList.add(topicInfo);
                }

                for (String notActiveTopicName : kafkaConsumerVo.getNotActiveTopicNames()) {
                    TreeInfo topicInfo = new TreeInfo(notActiveTopicName);
                    topicInfo.setItemStyle(TreeInfo.Style.warn());
                    topicInfo.setLineStyle(TreeInfo.Style.warn());
                    topicTreeInfoList.add(topicInfo);
                }
                consumerGroup.setChildren(topicTreeInfoList);
                consuerGroupTreeInfoList.add(consumerGroup);
            }
            root.setChildren(consuerGroupTreeInfoList);
        }
        if (root.getChildren() != null && root.getChildren().size() > 0) {
            return Result.ok(root);
        } else {
            return Result.ok();
        }
    }

    @PostMapping("listConsumerDetails")
    @ResponseBody
    public Result<List<KafkaTopicVo>> listConsumerDetails(@RequestParam(required = true, name = "groupId") String groupId) throws Exception {
        groupId = groupId.trim();
        List<KafkaTopicVo> result = new ArrayList<>();
        List<KafkaConsumerVo> kafkaConsumerVoList = kafkaConsumerService.listKafkaConsumers(groupId);

        if (kafkaConsumerVoList != null && kafkaConsumerVoList.size() > 0) {
            KafkaConsumerVo kafkaConsumerVo = kafkaConsumerVoList.get(0);

            for (String topicName : kafkaConsumerVo.getActiveTopicNames()) {
                KafkaTopicVo kafkaTopicVo = new KafkaTopicVo(topicName, 1);
                result.add(kafkaTopicVo);
            }

            for (String notActiveTopicName : kafkaConsumerVo.getNotActiveTopicNames()) {
                KafkaTopicVo kafkaTopicVo = new KafkaTopicVo(notActiveTopicName, 0);
                result.add(kafkaTopicVo);
            }

        }

        for (KafkaTopicVo kafkaTopicVo : result) {
            long lag = 0L;
            try {
                List<OffsetVo> offsetVoList = kafkaConsumerService.listOffsetVo(groupId, kafkaTopicVo.getTopicName());

                for (OffsetVo offsetVo : offsetVoList) {
                    if (offsetVo.getLag() != null && offsetVo.getLag() > 0) {
                        lag += offsetVo.getLag();
                    }
                    if (offsetVo.getLogSize() < 0L) {
                        kafkaTopicVo.setError(offsetVo.getConsumerId());
                    }
                }
            } catch (Exception ignored) {
                lag = -1L;
            }
            kafkaTopicVo.setLag(lag);
        }
        return Result.ok(result);
    }

    @PostMapping("listOffsetVo")
    @ResponseBody
    public Result<List<OffsetVo>> listOffsetVo(@RequestParam(required = true, name = "groupId") String groupId,
                                               @RequestParam(required = true, name = "topicName") String topicName) {
        groupId = groupId.trim();
        topicName = topicName.trim();
        try {
            return Result.ok(kafkaConsumerService.listOffsetVo(groupId, topicName));
        } catch (Exception ignored) {
            return Result.ok();
        }
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(required = true, name = "consumerGroupId") String consumerGroupId) {
        consumerGroupId = consumerGroupId.trim();
        kafkaConsumerService.delete(consumerGroupId.trim());
        return Result.ok();
    }
}
