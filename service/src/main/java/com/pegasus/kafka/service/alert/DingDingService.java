package com.pegasus.kafka.service.alert;

import com.alibaba.fastjson.JSON;
import com.pegasus.kafka.entity.dto.SysDingDingConfig;
import com.pegasus.kafka.entity.po.DingDingMessage;
import com.pegasus.kafka.service.dto.SysDingDingConfigService;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The service for sending message in DingDing.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class DingDingService {

    private static final String URL = "https://oapi.dingtalk.com/robot/send?access_token=%s&timestamp=%s&sign=%s";
    private final SysDingDingConfigService sysDingDingConfigService;

    public DingDingService(SysDingDingConfigService sysDingDingConfigService) {
        this.sysDingDingConfigService = sysDingDingConfigService;
    }

    public String send(DingDingMessage message) throws Exception {

        SysDingDingConfig sysDingDingConfig = sysDingDingConfigService.get();

        if (sysDingDingConfig == null) {
            return "";
        }

        Long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + sysDingDingConfig.getSecret();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(sysDingDingConfig.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        String WEBHOOK_TOKEN = String.format(URL, sysDingDingConfig.getAccessToken(), timestamp, sign);
        HttpClient httpclient = HttpClients.createDefault();

        HttpPost httppost = new HttpPost(WEBHOOK_TOKEN);
        httppost.addHeader("Content-Type", "application/json; charset=UTF-8");
        StringEntity se = new StringEntity(JSON.toJSONString(message), "UTF-8");
        httppost.setEntity(se);
        try {
            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}