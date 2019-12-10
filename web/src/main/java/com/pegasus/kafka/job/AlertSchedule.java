package com.pegasus.kafka.job;

import com.pegasus.kafka.common.utils.ZooKeeperKpiUtils;
import com.pegasus.kafka.entity.dto.SysAlertCluster;
import com.pegasus.kafka.entity.po.DingDingMessage;
import com.pegasus.kafka.entity.vo.KafkaBrokerVo;
import com.pegasus.kafka.service.alert.AlertService;
import com.pegasus.kafka.service.alert.DingDingService;
import com.pegasus.kafka.service.alert.MailService;
import com.pegasus.kafka.service.dto.SysAlertClusterService;
import com.pegasus.kafka.service.dto.SysAlertConsumerService;
import com.pegasus.kafka.service.kafka.KafkaBrokerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The schedule job for providing an alert when a problem is detected.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Component
public class AlertSchedule {
    private final SysAlertClusterService sysAlertClusterService;
    private final KafkaBrokerService kafkaBrokerService;
    private final DingDingService dingDingService;
    private final MailService mailService;
    private final AlertService alertService;


    public AlertSchedule(SysAlertConsumerService sysAlertConsumerService, SysAlertClusterService sysAlertClusterService, KafkaBrokerService kafkaBrokerService, DingDingService dingDingService, MailService mailService, AlertService alertService) {
        this.sysAlertClusterService = sysAlertClusterService;
        this.kafkaBrokerService = kafkaBrokerService;
        this.dingDingService = dingDingService;
        this.mailService = mailService;
        this.alertService = alertService;
    }

    @Scheduled(cron = "0/1 * * * * ?") //每秒执行一次
    public void alert() throws InterruptedException {
        AlertService.Alert alert = alertService.poll();
        if (alert == null) {
            return;
        }

        if (!StringUtils.isEmpty(alert.getEmail())) {
            try {
                mailService.send(alert.getEmail(), alert.getEmailTitle(), alert.getEmailContent());
            } catch (Exception ignored) {
            }
        }

        if (!StringUtils.isEmpty(alert.getDingContent())) {
            try {
                DingDingMessage message = new DingDingMessage();
                message.setMsgtype("text");
                message.setText(new DingDingMessage.Text(alert.getDingContent()));
                message.setAt(new DingDingMessage.At(Collections.singletonList(""), true));
                dingDingService.send(message);
            } catch (Exception ignored) {
            }
        }
    }

    @Scheduled(cron = "0/30 * * * * ?") //每30秒执行一次
    public void checkCluster() throws Exception {
        List<SysAlertCluster> list = sysAlertClusterService.list();
        if (list == null || list.size() < 1) {
            return;
        }
        List<KafkaBrokerVo> KafkaBrokerVofoList = kafkaBrokerService.listAllBrokers();
        List<SysAlertCluster> zooKeepers = list.stream().filter(p -> p.getType().equals(SysAlertCluster.Type.ZOOKEEPER.getCode())).collect(Collectors.toList());
        List<SysAlertCluster> kafkas = list.stream().filter(p -> p.getType().equals(SysAlertCluster.Type.KAFKA.getCode())).collect(Collectors.toList());


        for (SysAlertCluster zooKeeper : zooKeepers) {
            String[] split = zooKeeper.getServer().split(":");
            String ip = split[0];
            int port = Integer.parseInt(split[1]);

            ZooKeeperKpiUtils.ZooKeeperKpi zooKeeperKpi = ZooKeeperKpiUtils.listKpi(ip, port);
            if (StringUtils.isEmpty(zooKeeperKpi.getZkNumAliveConnections())) {
                AlertService.Alert alert = new AlertService.Alert();
                alert.setEmail(zooKeeper.getEmail());
                alert.setEmailTitle(String.format("ZOOKEEPER主机[%s]不可用, 请检查.", ip));
                alert.setDingContent(alert.getEmailTitle());
                alert.setDingContent(alert.getEmailTitle());
                alertService.offer(alert);
            }
        }

        for (SysAlertCluster kafka : kafkas) {
            String[] split = kafka.getServer().split(":");
            String ip = split[0];
            String port = split[1];
            List<KafkaBrokerVo> result = KafkaBrokerVofoList.stream().filter(p -> p.getHost().equals(ip) && p.getPort().equals(port)).collect(Collectors.toList());
            if (result.size() < 1) {
                AlertService.Alert alert = new AlertService.Alert();
                alert.setEmail(kafka.getEmail());
                alert.setEmailTitle(String.format("KAFKA主机[%s]不可用, 请检查.", ip));
                alert.setDingContent(alert.getEmailTitle());
                alert.setDingContent(alert.getEmailTitle());
                alertService.offer(alert);
            }
        }
    }
}