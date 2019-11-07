package com.pegasus.kafka.service.alert;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The service for providing an alert when a problem is detected.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class AlertService {

    private ArrayBlockingQueue<Alert> alertList;

    public AlertService() {
        alertList = new ArrayBlockingQueue<>(1024);
    }

    public boolean offer(String dingContent) {
        Alert alert = new Alert();
        alert.setEmail(null);
        alert.setEmailTitle(null);
        alert.setEmailContent(null);
        alert.setDingContent(dingContent);
        return alertList.offer(alert);
    }

    public boolean offer(String email, String emailTitle, String emailContent) {
        Alert alert = new Alert();
        alert.setEmail(email);
        alert.setEmailTitle(emailTitle);
        alert.setEmailContent(emailContent);
        alert.setDingContent(null);
        return alertList.offer(alert);
    }

    public boolean offer(String email, String emailTitle, String emailContent, String dingContent) {
        Alert alert = new Alert();
        alert.setEmail(email);
        alert.setEmailTitle(emailTitle);
        alert.setEmailContent(emailContent);
        alert.setDingContent(dingContent);
        return alertList.offer(alert);
    }

    public boolean offer(Alert alert) {
        return alertList.offer(alert);
    }

    public Alert poll() throws InterruptedException {
        return alertList.poll(5, TimeUnit.MILLISECONDS);
    }

    @Data
    public static class Alert {
        private String email;
        private String emailTitle;
        private String emailContent;
        private String dingContent;
    }

}
