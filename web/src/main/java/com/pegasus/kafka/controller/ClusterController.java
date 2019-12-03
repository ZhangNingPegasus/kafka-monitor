package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.echarts.TreeInfo;
import com.pegasus.kafka.entity.vo.KafkaBrokerInfo;
import com.pegasus.kafka.entity.vo.ZooKeeperInfo;
import com.pegasus.kafka.service.core.KafkaZkService;
import com.pegasus.kafka.service.kafka.KafkaBrokerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

import static com.pegasus.kafka.controller.ClusterController.PREFIX;

/**
 * The controller for providing the ability of cluster.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class ClusterController {
    public static final String PREFIX = "cluster";
    private final KafkaBrokerService kafkaClusterService;
    private final KafkaZkService kafkaZkService;

    public ClusterController(KafkaBrokerService kafkaClusterService, KafkaZkService kafkaZkService) {
        this.kafkaClusterService = kafkaClusterService;
        this.kafkaZkService = kafkaZkService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<KafkaBrokerInfo>> list() throws Exception {
        return Result.ok(kafkaClusterService.listAllBrokers());
    }

    @PostMapping("listZk")
    @ResponseBody
    public Result<List<ZooKeeperInfo>> listZk() {
        return Result.ok(kafkaZkService.listZooKeeperCluster());
    }

    @PostMapping("getChartData")
    @ResponseBody
    public Result<TreeInfo> getChartData() throws Exception {
        TreeInfo root = new TreeInfo("Kafka集群");

        List<KafkaBrokerInfo> allBrokers = kafkaClusterService.listAllBrokers();

        List<TreeInfo> children = new ArrayList<>(allBrokers.size());
        for (KafkaBrokerInfo broker : allBrokers) {
            TreeInfo treeInfo = new TreeInfo(String.format("[%s] : %s", broker.getName(), broker.getHost()));
            treeInfo.setStyle(TreeInfo.Style.success());
            children.add(treeInfo);
        }
        if (children.size() > 0) {
            root.setChildren(children);
        }
        return Result.ok(root);
    }
}