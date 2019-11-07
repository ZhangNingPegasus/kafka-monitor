package com.pegasus.kafka.entity.po;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * The paramter object for DingDing Message. Using for send the message in Ding Ding.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Data
public class DingDingMessage implements Serializable {
    private String msgtype;
    private Text text;
    private At at;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Text {
        private String content;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class At {
        private List<String> atMobiles;
        private Boolean isAtAll;
    }

}


