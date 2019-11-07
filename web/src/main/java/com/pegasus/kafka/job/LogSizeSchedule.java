package com.pegasus.kafka.job;

import com.pegasus.kafka.entity.dto.SysAlertConsumer;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.service.alert.AlertService;
import com.pegasus.kafka.service.dto.SysAlertConsumerService;
import com.pegasus.kafka.service.dto.SysLagService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The schedule job for collection log size of topics.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Component
public class LogSizeSchedule {
    private final SysLogSizeService sysLogSizeService;
    private final SysLagService sysLagService;
    private final SysAlertConsumerService sysAlertConsumerService;
    private final AlertService alertService;

    public LogSizeSchedule(SysLogSizeService sysLogSizeService, SysLagService sysLagService, SysAlertConsumerService sysAlertConsumerService, AlertService alertService) {
        this.sysLogSizeService = sysLogSizeService;
        this.sysLagService = sysLagService;
        this.sysAlertConsumerService = sysAlertConsumerService;
        this.alertService = alertService;
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void collect() throws Exception {
        Date now = new Date();
        SysLogSizeService.Matrix matrix = sysLogSizeService.kpi(now);
        try {
            sysLogSizeService.saveBatch(matrix.getSysLogSizeList());
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        try {
            sysLagService.saveBatch(matrix.getSysLagList());
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        List<SysAlertConsumer> sysAlertConsumerList = sysAlertConsumerService.list();

        for (SysAlertConsumer sysAlertConsumer : sysAlertConsumerList) {
            String consumerName = sysAlertConsumer.getGroupId();
            String topicName = sysAlertConsumer.getTopicName();

            List<SysLag> sysLagList = matrix.getSysLagList().stream().filter(p -> p.getConsumerName().equals(consumerName) && p.getTopicName().equals(topicName)).collect(Collectors.toList());

            for (SysLag sysLag : sysLagList) {
                if (sysLag.getLag() > sysAlertConsumer.getLagThreshold()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    AlertService.Alert alert = new AlertService.Alert();
                    alert.setEmail(sysAlertConsumer.getEmail());
                    alert.setEmailTitle(String.format("消费组[%s]订阅的主题[%s]堆积的消息量已超过阀值%s, 现有积压消息量%s", consumerName, topicName, sysAlertConsumer.getLagThreshold(), sysLag.getLag()));
                    alert.setEmailContent("告警主机：" + InetAddress.getLocalHost().getHostName() + "<br/>" +
                            "主机地址：" + InetAddress.getLocalHost().getHostAddress() + "<br/>" +
                            "告警等级：警告<br/>" +
                            "当前状态：OK<br/>" +
                            "问题详情：" + alert.getEmailTitle() + "<br/>" +
                            "告警时间：" + sdf.format(now) + "<br/>");
                    alert.setDingContent("告警主机：" + InetAddress.getLocalHost().getHostName() + "\n" +
                            "主机地址：" + InetAddress.getLocalHost().getHostAddress() + "\n" +
                            "告警等级：警告\n" +
                            "当前状态：OK\n" +
                            "问题详情：" + alert.getEmailTitle() + "\n" +
                            "告警时间：" + sdf.format(now) + "\n");
                    alertService.offer(alert);
                }
            }
        }
    }
}