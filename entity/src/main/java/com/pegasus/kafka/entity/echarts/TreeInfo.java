package com.pegasus.kafka.entity.echarts;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.List;

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

    public TreeInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Style getItemStyle() {
        return itemStyle;
    }

    public void setItemStyle(Style itemStyle) {
        this.itemStyle = itemStyle;
    }

    public Style getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(Style lineStyle) {
        this.lineStyle = lineStyle;
    }

    public List<TreeInfo> getChildren() {
        return children;
    }

    public void setChildren(List<TreeInfo> children) {
        this.children = children;
    }

    public void setStyle(Style style) {
        this.setItemStyle(style);
        this.setLineStyle(style);
    }

    public static class Style {
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

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(String borderColor) {
            this.borderColor = borderColor;
        }
    }
}
