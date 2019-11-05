package com.pegasus.kafka.entity.po;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

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


