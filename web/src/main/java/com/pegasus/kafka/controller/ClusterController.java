package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.vo.KafkaBrokerInfo;
import com.pegasus.kafka.service.kafka.KafkaClusterService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
@RequestMapping("cluster")
public class ClusterController {
    private final KafkaClusterService kafkaClusterService;

    public ClusterController(KafkaClusterService kafkaClusterService) {
        this.kafkaClusterService = kafkaClusterService;
    }

    @RequestMapping("tolist")
    public String toList() {
        return "cluster/list";
    }

    @RequestMapping("list")
    @ResponseBody
    public Result<List<KafkaBrokerInfo>> list() throws Exception {
        return Result.success(kafkaClusterService.getAllBrokers());
    }
}