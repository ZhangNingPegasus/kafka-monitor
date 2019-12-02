package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.common.utils.ZooKeeperKpiUtils;
import com.pegasus.kafka.entity.vo.ZooKeeperInfo;
import com.pegasus.kafka.service.core.KafkaZkService;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

import static com.pegasus.kafka.controller.ZkCliController.PREFIX;

/**
 * The controller for providing the zookeeper's client.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class ZkCliController {
    public static final String PREFIX = "zkCli";
    private final KafkaZkService kafkaZkService;

    public ZkCliController(KafkaZkService kafkaZkService) {
        this.kafkaZkService = kafkaZkService;
    }

    @RequestMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @PostMapping("zkInfo")
    @ResponseBody
    public Result<String> zkInfo() {
        try {
            List<ZooKeeperInfo> zooKeeperInfos = kafkaZkService.listZooKeeperCluster();
            if (zooKeeperInfos.size() > 0) {
                ZooKeeperInfo zooKeeperInfo = zooKeeperInfos.get(0);
                ZooKeeperKpiUtils.ZooKeeperKpi zooKeeperKpi = ZooKeeperKpiUtils.listKpi(zooKeeperInfo.getHost(), Integer.parseInt(zooKeeperInfo.getPort()));
                if (!StringUtils.isEmpty(zooKeeperKpi.getZkNumAliveConnections())) {
                    List<String> result = zooKeeperInfos.stream().map(p -> String.format("%s:%s", p.getHost(), p.getPort())).collect(Collectors.toList());
                    return Result.success(result.toString());
                }
            }
        } catch (Exception ignored) {
        }
        return Result.error();
    }

    @PostMapping("execute")
    @ResponseBody
    public Result<String> execute(@RequestParam(name = "command", required = true) String command,
                                  @RequestParam(name = "type", required = true) String type) throws Exception {
        try {
            return Result.success(kafkaZkService.execute(command, type));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}