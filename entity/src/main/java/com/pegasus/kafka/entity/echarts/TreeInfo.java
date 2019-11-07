package com.pegasus.kafka.entity.echarts;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * The entity for ajax's response. Using for echart's tree graph.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class TreeInfo implements Serializable {
    private String name;
    private Integer value;
    private Style itemStyle;
    private Style lineStyle;
    private List<TreeInfo> children;

    public TreeInfo(String name) {
        this.name = name;
    }

    public void setStyle(Style style) {
        this.setItemStyle(style);
        this.setLineStyle(style);
    }

    @Data
    @JsonSerialize
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class Style implements Serializable {
        private String color;
        private String borderColor;

        private Style(String color, String borderColor) {
            this.color = color;
            this.borderColor = borderColor;
        }

        public static Style warn() {
            return new Style("#FFB800", "#FFB800");
        }

        public static Style success() {
            return new Style("#009688", "#009688");
        }

        public static Style info() {
            return new Style("#cccccc", "#cccccc");
        }
    }
}
