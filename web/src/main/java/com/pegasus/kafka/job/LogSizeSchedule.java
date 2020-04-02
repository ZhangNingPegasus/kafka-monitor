package com.pegasus.kafka.job;

import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysAlertConsumer;
import com.pegasus.kafka.entity.dto.SysAlertTopic;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.entity.dto.SysLogSize;
import com.pegasus.kafka.service.alert.AlertService;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.core.ThreadService;
import com.pegasus.kafka.service.dto.SysAlertConsumerService;
import com.pegasus.kafka.service.dto.SysAlertTopicService;
import com.pegasus.kafka.service.dto.SysLagService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import lombok.extern.java.Log;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The schedule job for collection log size of topics.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Log
@Component
public class LogSizeSchedule {
    private final KafkaService kafkaService;
    private final SysLogSizeService sysLogSizeService;
    private final SysLagService sysLagService;
    private final SysAlertConsumerService sysAlertConsumerService;
    private final SysAlertTopicService sysAlertTopicService;
    private final AlertService alertService;
    final ThreadService threadService;
    private final ThreadLocal<SimpleDateFormat> threadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    public LogSizeSchedule(KafkaService kafkaService, SysLogSizeService sysLogSizeService,
                           SysLagService sysLagService,
                           SysAlertConsumerService sysAlertConsumerService,
                           SysAlertTopicService sysAlertTopicService, AlertService alertService,
                           ThreadService threadService) {
        this.kafkaService = kafkaService;
        this.sysLogSizeService = sysLogSizeService;
        this.sysLagService = sysLagService;
        this.sysAlertConsumerService = sysAlertConsumerService;
        this.sysAlertTopicService = sysAlertTopicService;
        this.alertService = alertService;
        this.threadService = threadService;
    }

    //每1分钟执行一次
    @Scheduled(cron = "0 0/1 * * * ?")
    public void collect() throws Exception {
        Date now = new Date();
        SysLogSizeService.Matrix matrix = sysLogSizeService.kpi(now);
        try {
            sysLogSizeService.batchSave(matrix.getSysLogSizeList());
        } catch (Exception e) {
            log.warning(e.getMessage());
            e.printStackTrace();
        }

        try {
            sysLagService.batchSave(matrix.getSysLagList());
        } catch (Exception e) {
            log.warning(e.getMessage());
            e.printStackTrace();
        }

        String kafkaUrl = kafkaService.getBootstrapServers(false);

        threadService.submit(() -> {
            try {
                lagAlert(matrix, now, kafkaUrl);
            } catch (Exception e) {
                log.warning(e.getMessage());
                e.printStackTrace();
            }
        });

        threadService.submit(() -> {
            try {
                tpsAlert(kafkaUrl);
            } catch (Exception e) {
                log.warning(e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void lagAlert(SysLogSizeService.Matrix matrix, Date now, String kafkaUrl) throws Exception {
        List<SysAlertConsumer> sysAlertConsumerList = sysAlertConsumerService.list();
        if (sysAlertConsumerList == null || sysAlertConsumerList.size() < 1) {
            return;
        }

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
                    alert.setEmailContent("告警主机：" + kafkaUrl + "<br/>" +
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
                    alertService.offer(String.format("alert_lag_topic_%s", sysAlertConsumer.getId()), alert);
                }
            }
        }
    }

    private void tpsAlert(String kafkaUrl) throws Exception {
        List<SysAlertTopic> sysAlertTopicList = sysAlertTopicService.list();
        if (sysAlertTopicList == null || sysAlertTopicList.size() < 1) {
            return;
        }

        Date now = new Date();
        Date from = DateUtils.addMinutes(now, -4);
        Map<String, List<SysLogSize>> sysLogSizeMap = sysLogSizeService.listByTopicNames(sysAlertTopicList.stream().map(SysAlertTopic::getTopicName).collect(Collectors.toList()), from, now);

        for (SysAlertTopic sysAlertTopic : sysAlertTopicList) {
            Date start = toDate(sysAlertTopic.getFromTime());
            Date end = toDate(sysAlertTopic.getToTime());

            String topicName = sysAlertTopic.getTopicName();

            if (!sysLogSizeMap.containsKey(topicName)) {
                AlertService.Alert alert = new AlertService.Alert();
                alert.setEmail(sysAlertTopic.getEmail());
                alert.setEmailTitle(String.format("主题[%s]设置了TPS警告, %s, 但目前没有检测到任何数据", topicName, sysAlertTopic.toInfo()));
                alert.setEmailContent("告警主机：Kafka集群<br/>" +
                        "主机地址：" + kafkaUrl + "<br/>" +
                        "告警等级：警告<br/>" +
                        "当前状态：OK<br/>" +
                        "问题详情：" + alert.getEmailTitle() + "<br/>" +
                        "告警时间：" + Common.format(now) + "<br/>");
                alert.setDingContent("告警主机：Kafka集群\n" +
                        "主机地址：" + kafkaUrl + "\n" +
                        "告警等级：警告\n" +
                        "当前状态：OK\n" +
                        "问题详情：" + alert.getEmailTitle() + "\n" +
                        "告警时间：" + Common.format(now) + "\n");
                alertService.offer(String.format("alert_no_data_topic_%s", sysAlertTopic.getId()), alert);
                continue;
            }

            List<SysLogSize> sysLogSizeList = sysLogSizeMap.get(topicName);

            if ((start != null && sysLogSizeList.get(0).getCreateTime().before(start)) || (end != null && sysLogSizeList.get(0).getCreateTime().after(end))) {
                continue;
            }

            Integer tps = null;
            Integer momTps = null;
            if (sysLogSizeList.size() > 1) {
                SysLogSize s2 = sysLogSizeList.get(0);
                SysLogSize s1 = sysLogSizeList.get(1);
                tps = (int) ((s2.getLogSize() - s1.getLogSize()) / ((s2.getCreateTime().getTime() - s1.getCreateTime().getTime()) / 1000.0));
            }

            if (sysLogSizeList.size() > 2) {
                SysLogSize s3 = sysLogSizeList.get(0);
                SysLogSize s2 = sysLogSizeList.get(1);
                SysLogSize s1 = sysLogSizeList.get(2);
                int a = (int) ((s3.getLogSize() - s2.getLogSize()) / ((s3.getCreateTime().getTime() - s2.getCreateTime().getTime()) / 1000.0));
                int b = (int) ((s2.getLogSize() - s1.getLogSize()) / ((s2.getCreateTime().getTime() - s1.getCreateTime().getTime()) / 1000.0));
                momTps = a - b;
            }

            boolean tpsAlert = false;
            boolean momTpsAlert = false;
            if (tps != null && sysAlertTopic.getFromTps() != null) {
                if (tps < sysAlertTopic.getFromTps()) {
                    tpsAlert = true;
                }
            }

            if (tps != null && sysAlertTopic.getToTps() != null) {
                if (tps > sysAlertTopic.getToTps()) {
                    tpsAlert = true;
                }
            }
            if (momTps != null && sysAlertTopic.getFromMomTps() != null) {
                if (momTps < sysAlertTopic.getFromMomTps()) {
                    momTpsAlert = true;
                }
            }
            if (momTps != null && sysAlertTopic.getToMomTps() != null) {
                if (momTps > sysAlertTopic.getToMomTps()) {
                    momTpsAlert = true;
                }
            }

            if (tpsAlert) {
                AlertService.Alert alert = new AlertService.Alert();
                alert.setEmail(sysAlertTopic.getEmail());
                alert.setEmailTitle(String.format("主题[%s]当前的TPS是: %s, 不满足设定: %s", topicName, tps, sysAlertTopic.toTpsInfo()));
                alert.setEmailContent("告警主机：Kafka集群<br/>" +
                        "主机地址：" + kafkaUrl + "<br/>" +
                        "告警等级：警告<br/>" +
                        "当前状态：OK<br/>" +
                        "问题详情：" + alert.getEmailTitle() + "<br/>" +
                        "告警时间：" + Common.format(now) + "<br/>");
                alert.setDingContent("告警主机：Kafka集群\n" +
                        "主机地址：" + kafkaUrl + "\n" +
                        "告警等级：警告\n" +
                        "当前状态：OK\n" +
                        "问题详情：" + alert.getEmailTitle() + "\n" +
                        "告警时间：" + Common.format(now) + "\n");
                alertService.offer(String.format("alert_tps_topic_%s", sysAlertTopic.getId()), alert);
            }

            if (momTpsAlert) {
                AlertService.Alert alert = new AlertService.Alert();
                alert.setEmail(sysAlertTopic.getEmail());
                alert.setEmailTitle(String.format("主题[%s]当前的TPS变化是: %s, 不满足设定: %s", topicName, momTps, sysAlertTopic.toMomTpsInfo()));
                alert.setEmailContent("告警主机：Kafka集群<br/>" +
                        "主机地址：" + kafkaUrl + "<br/>" +
                        "告警等级：警告<br/>" +
                        "当前状态：OK<br/>" +
                        "问题详情：" + alert.getEmailTitle() + "<br/>" +
                        "告警时间：" + Common.format(now) + "<br/>");
                alert.setDingContent("告警主机：Kafka集群\n" +
                        "主机地址：" + kafkaUrl + "\n" +
                        "告警等级：警告\n" +
                        "当前状态：OK\n" +
                        "问题详情：" + alert.getEmailTitle() + "\n" +
                        "告警时间：" + Common.format(now) + "\n");
                alertService.offer(String.format("alert_mon_tps_topic_%s", sysAlertTopic.getId()), alert);
            }
        }
    }

    private Date toDate(String time) throws ParseException {
        if (StringUtils.isEmpty(time)) {
            return null;
        }
        String today = threadLocal.get().format(new Date());
        return Common.parse(String.format("%s %s", today, time));
    }

}