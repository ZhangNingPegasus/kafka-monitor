package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.vo.ZooKeeperInfo;
import com.pegasus.kafka.service.core.KafkaZkService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("zkCli")
public class ZkCliController {

    private final KafkaZkService kafkaZkService;

    public ZkCliController(KafkaZkService kafkaZkService) {
        this.kafkaZkService = kafkaZkService;
    }

    @RequestMapping("tolist")
    public String toList() {
        return "zkCli/list";
    }

    @RequestMapping("zkInfo")
    @ResponseBody
    public Result<String> zkInfo() {
        try {
            List<ZooKeeperInfo> zooKeeperInfos = kafkaZkService.listZooKeeperCluster();
            List<String> result = zooKeeperInfos.stream().map(p -> String.format("%s:%s", p.getHost(), p.getPort())).collect(Collectors.toList());
            return Result.success(result.toString());
        } catch (Exception e) {
            return Result.error();
        }
    }

    @RequestMapping("execute")
    @ResponseBody
    public Result<String> execute(@RequestParam(name = "command", required = true) String command,
                                  @RequestParam(name = "type", required = true) String type) throws Exception {
        return Result.success(kafkaZkService.execute(command, type));
    }

}