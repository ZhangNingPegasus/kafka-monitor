package com.pegasus.kafka.entity.echarts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LineInfo implements Serializable {
    private List<String> topicNames;
    private List<String> times;
    private List<Series> series;

    @Getter
    @Setter
    @JsonSerialize
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class Series implements Serializable {
        private String name;
        private List<Double> data;
        private String type;
        private Boolean smooth;
        private JSONObject areaStyle;
    }


}
