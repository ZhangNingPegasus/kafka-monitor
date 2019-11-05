package com.pegasus.kafka;

import com.pegasus.kafka.entity.po.DingDingMessage;
import com.pegasus.kafka.service.alert.DingDingService;
import com.pegasus.kafka.service.alert.MailService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaServiceTest {

    @Autowired
    private MailService mailService;
    @Autowired
    private DingDingService dingDingService;

    @Test
    public void test1() throws Exception {
        mailService.send("349409664@qq.com", "测试", "来一个测试, Have a Testing.");
    }

    @Test
    public void test2() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DingDingMessage msg = new DingDingMessage();
        msg.setMsgtype("text");
        msg.setText(new DingDingMessage.Text("告警主机：" + InetAddress.getLocalHost().getHostName() + "\n" +
                "主机地址：" + InetAddress.getLocalHost().getHostAddress() + "\n" +
                "告警等级：警告\n" +
                "当前状态：OK\n" +
                "问题详情：消息积压堆积已超过阀值\n" +
                "告警时间：" + sdf.format(new Date()) + "\n"));
        msg.setAt(new DingDingMessage.At(Arrays.asList(""), true));

        dingDingService.send(msg);
    }

}
