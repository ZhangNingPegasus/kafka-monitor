package com.pegasus.kafka.service.alert;

import com.pegasus.kafka.entity.dto.SysMailConfig;
import com.pegasus.kafka.service.dto.SysMailConfigService;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * The service for sending email.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class MailService {

    private final SysMailConfigService sysMailConfigService;

    public MailService(SysMailConfigService sysMailConfigService) {
        this.sysMailConfigService = sysMailConfigService;
    }

    public void send(String to, String subject, String html) throws Exception {
        SysMailConfig sysMailConfig = sysMailConfigService.get();
        if (sysMailConfig == null) {
            return;
        }

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", sysMailConfig.getHost());
        props.setProperty("mail.port", sysMailConfig.getPort());
        props.setProperty("mail.smtp.auth", "true");
        Session session = Session.getInstance(props);
        session.setDebug(false);
        MimeMessage message = new MimeMessage(session);

        //设置邮件的头
        message.setFrom(new InternetAddress(sysMailConfig.getUsername()));
        message.setRecipients(Message.RecipientType.TO, new Address[]{new InternetAddress(to)});

        message.setSubject(subject, "UTF-8");
        //设置正文
        message.setContent(html, "text/html;charset=utf-8");
        message.saveChanges();

        //发送邮件
        Transport ts = session.getTransport();
        ts.connect(sysMailConfig.getUsername(), sysMailConfig.getPassword());
        ts.sendMessage(message, message.getAllRecipients());

    }

}