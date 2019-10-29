package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.echarts.TreeInfo;
import com.pegasus.kafka.entity.vo.KafkaConsumerInfo;
import com.pegasus.kafka.entity.vo.KafkaTopicInfo;
import com.pegasus.kafka.entity.vo.OffsetInfo;
import com.pegasus.kafka.service.kafka.KafkaConsumerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("consumer")
public class ConsumerController {

    private final KafkaConsumerService kafkaConsumerService;

    public ConsumerController(KafkaConsumerService kafkaConsumerService) {
        this.kafkaConsumerService = kafkaConsumerService;
    }

    @RequestMapping("tolist")
    public String toList() {
        return "consumer/list";
    }

    @RequestMapping("todetail/{groupId}")
    public String toDetail(Model model, @PathVariable(name = "groupId", required = true) String groupId) {
        groupId = groupId.trim();
        model.addAttribute("groupId", groupId);
        return "consumer/detail";
    }

    @RequestMapping("list")
    @ResponseBody
    public Result<List<KafkaConsumerInfo>> list(HttpSession httpSession) {
        try {
            List<KafkaConsumerInfo> kafkaConsumerInfos = kafkaConsumerService.listKafkaConsumers();
            httpSession.setAttribute(Constants.SESSION_KAFKA_CONSUMER_INFO, kafkaConsumerInfos);
            return Result.success(kafkaConsumerInfos);
        } catch (Exception e) {
            return Result.success();
        }
    }

    @RequestMapping("getChartData")
    @ResponseBody
    public Result<TreeInfo> getChartData(HttpSession httpSession) {
        TreeInfo root = new TreeInfo("消费者 - 主题");
        root.setStyle(TreeInfo.Style.info());
        List<KafkaConsumerInfo> kafkaConsumerInfos = (List<KafkaConsumerInfo>) httpSession.getAttribute(Constants.SESSION_KAFKA_CONSUMER_INFO);
        httpSession.removeAttribute(Constants.SESSION_KAFKA_CONSUMER_INFO);
        if (kafkaConsumerInfos != null) {
            List<TreeInfo> consuerGroupTreeInfoList = new ArrayList<>(kafkaConsumerInfos.size());

            for (KafkaConsumerInfo kafkaConsumerInfo : kafkaConsumerInfos) {
                TreeInfo consumerGroup = new TreeInfo(kafkaConsumerInfo.getGroupId());

                if (kafkaConsumerInfo.getNotActiveTopicNames().size() == kafkaConsumerInfo.getTopicNames().size()) {
                    consumerGroup.setStyle(TreeInfo.Style.warn());
                } else if (kafkaConsumerInfo.getActiveTopicNames().size() == kafkaConsumerInfo.getTopicNames().size()) {
                    consumerGroup.setStyle(TreeInfo.Style.success());
                } else {
                    consumerGroup.setStyle(TreeInfo.Style.info());
                }

                List<TreeInfo> topicTreeInfoList = new ArrayList<>(kafkaConsumerInfo.getTopicCount());
                for (String activeTopicName : kafkaConsumerInfo.getActiveTopicNames()) {
                    TreeInfo topicInfo = new TreeInfo(activeTopicName);
                    topicInfo.setStyle(TreeInfo.Style.success());
                    topicTreeInfoList.add(topicInfo);
                }

                for (String notActiveTopicName : kafkaConsumerInfo.getNotActiveTopicNames()) {
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
            return Result.success(root);
        } else {
            return Result.success();
        }
    }

    @RequestMapping("todetail/listConsumerDetails/{groupId}")
    @ResponseBody
    public Result<List<KafkaTopicInfo>> listConsumerDetails(@PathVariable(required = true, name = "groupId") String groupId) throws Exception {
        groupId = groupId.trim();
        List<KafkaTopicInfo> result = new ArrayList<>();
        List<KafkaConsumerInfo> kafkaConsumerInfos = kafkaConsumerService.listKafkaConsumers(groupId);

        if (kafkaConsumerInfos != null && kafkaConsumerInfos.size() > 0) {
            KafkaConsumerInfo kafkaConsumerInfo = kafkaConsumerInfos.get(0);

            for (String topicName : kafkaConsumerInfo.getActiveTopicNames()) {
                KafkaTopicInfo kafkaTopicInfo = new KafkaTopicInfo(topicName, 1);
                result.add(kafkaTopicInfo);
            }

            for (String notActiveTopicName : kafkaConsumerInfo.getNotActiveTopicNames()) {
                KafkaTopicInfo kafkaTopicInfo = new KafkaTopicInfo(notActiveTopicName, 0);
                result.add(kafkaTopicInfo);
            }

        }

        for (KafkaTopicInfo kafkaTopicInfo : result) {
            Long lag = 0L;
            try {
                List<OffsetInfo> offsetInfos = kafkaConsumerService.listOffsetInfo(groupId, kafkaTopicInfo.getTopicName());

                for (OffsetInfo offsetInfo : offsetInfos) {
                    if (offsetInfo.getLag() != null && offsetInfo.getLag() > 0) {
                        lag += offsetInfo.getLag();
                    }
                    if (offsetInfo.getLogSize() < 0L) {
                        kafkaTopicInfo.setError(offsetInfo.getConsumerId());
                    }
                }
            } catch (Exception ignored) {
                lag = -1L;
            }

            kafkaTopicInfo.setLag(lag);
        }

        return Result.success(result);
    }

    @RequestMapping("todetail/listOffsetInfo/{groupId}/{topicName}")
    @ResponseBody
    public Result<List<OffsetInfo>> listOffsetInfo(@PathVariable(required = true, name = "groupId") String groupId,
                                                   @PathVariable(required = true, name = "topicName") String topicName) {
        groupId = groupId.trim();
        topicName = topicName.trim();
        try {
            return Result.success(kafkaConsumerService.listOffsetInfo(groupId, topicName));
        } catch (Exception ignored) {
            return Result.success();
        }
    }

    @RequestMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(required = true, name = "consumerGroupId") String consumerGroupId) {
        consumerGroupId = consumerGroupId.trim();
        kafkaConsumerService.delete(consumerGroupId.trim());
        return Result.success();
    }
}
