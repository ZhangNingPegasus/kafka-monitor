package com.pegasus.kafka.job;

import com.pegasus.kafka.entity.dto.SysAlertConsumer;
import com.pegasus.kafka.entity.dto.SysLag;
import com.pegasus.kafka.entity.po.DingDingMessage;
import com.pegasus.kafka.service.alert.DingDingService;
import com.pegasus.kafka.service.alert.MailService;
import com.pegasus.kafka.service.dto.SysAlertConsumerService;
import com.pegasus.kafka.service.dto.SysLagService;
import com.pegasus.kafka.service.dto.SysLogSizeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component

public class LogSizeSchedule {
    private final SysLogSizeService sysLogSizeService;
    private final SysLagService sysLagService;
    private final SysAlertConsumerService sysAlertConsumerService;
    private final DingDingService dingDingService;
    private final MailService mailService;


    public LogSizeSchedule(SysLogSizeService sysLogSizeService, SysLagService sysLagService, SysAlertConsumerService sysAlertConsumerService, DingDingService dingDingService, MailService mailService) {
        this.sysLogSizeService = sysLogSizeService;
        this.sysLagService = sysLagService;
        this.sysAlertConsumerService = sysAlertConsumerService;
        this.dingDingService = dingDingService;
        this.mailService = mailService;
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void collect() throws Exception {
        SysLogSizeService.Matrix matrix = sysLogSizeService.kpi(new Date());
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
                    String email = sysAlertConsumer.getEmail();
                    String content = String.format("消费组[%s]订阅的主题[%s]堆积的消息量已超过阀值%s, 现有积压消息量%s", consumerName, topicName, sysAlertConsumer.getLagThreshold(), sysLag.getLag());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (!StringUtils.isEmpty(email)) {
                        try {
                            mailService.send(email, content, "告警主机：" + InetAddress.getLocalHost().getHostName() + "<br/>" +
                                    "主机地址：" + InetAddress.getLocalHost().getHostAddress() + "<br/>" +
                                    "告警等级：警告<br/>" +
                                    "当前状态：OK<br/>" +
                                    "问题详情：" + content + "<br/>" +
                                    "告警时间：" + sdf.format(new Date()) + "<br/>");
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        DingDingMessage message = new DingDingMessage();
                        message.setMsgtype("text");
                        message.setText(new DingDingMessage.Text("告警主机：" + InetAddress.getLocalHost().getHostName() + "\n" +
                                "主机地址：" + InetAddress.getLocalHost().getHostAddress() + "\n" +
                                "告警等级：警告\n" +
                                "当前状态：OK\n" +
                                "问题详情：" + content + "\n" +
                                "告警时间：" + sdf.format(new Date()) + "\n"));
                        message.setAt(new DingDingMessage.At(Arrays.asList(""), true));
                        dingDingService.send(message);
                    } catch (Exception ignored) {

                    }
                }
            }
        }

    }
}