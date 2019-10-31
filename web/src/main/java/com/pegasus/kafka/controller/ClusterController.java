package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.echarts.TreeInfo;
import com.pegasus.kafka.entity.vo.KafkaBrokerInfo;
import com.pegasus.kafka.service.kafka.KafkaBrokerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("cluster")
public class ClusterController {
    private final KafkaBrokerService kafkaClusterService;

    public ClusterController(KafkaBrokerService kafkaClusterService) {
        this.kafkaClusterService = kafkaClusterService;
    }

    @RequestMapping("tolist")
    public String toList() {
        return "cluster/list";
    }

    @RequestMapping("list")
    @ResponseBody
    public Result<List<KafkaBrokerInfo>> list() throws Exception {
        return Result.success(kafkaClusterService.listAllBrokers());
    }

    @RequestMapping("getChartData")
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
        if (children != null && children.size() > 0) {
            root.setChildren(children);
        }
        return Result.success(root);
    }
}