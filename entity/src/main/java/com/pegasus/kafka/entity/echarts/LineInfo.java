package com.pegasus.kafka.entity.echarts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * The entity for ajax's response. Using for echart's line graph.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class LineInfo implements Serializable {
    private List<String> topicNames;
    private List<String> times;
    private List<Series> series;

    @Data
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
