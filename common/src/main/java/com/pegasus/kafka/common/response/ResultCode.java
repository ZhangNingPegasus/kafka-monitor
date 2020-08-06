package com.pegasus.kafka.common.response;

import lombok.Getter;

/**
 * the enum for ajax's result
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Getter
public enum ResultCode {
    SUCCESS(0, true, "success"),
    ERROR(1, false, "error"),
    ZOOKEEPER_CONFIG_IS_NULL(2, false, "zookeeper的地址配置为空，请在application.yml中通过kafka.monitor.zookeeper配置zookeeper的地址，多个地址用逗号分隔，例:192.168.182.128:2181,192.168.182.129:2181,192.168.182.130:2181"),
    KAFKA_NOT_RUNNING(3, false, "KAFKA可能没有启动，请先启动kafka"),
    TOPIC_IS_RUNNING(4, false, "主题正在使用中"),
    CONSUMER_IS_RUNNING(5, false, "消费者正在消费消息中"),
    TOPIC_ALREADY_EXISTS(6, false, "该主题已存在"),
    TOPIC_NOT_EXISTS(7, false, "主题不存在");

    private int code;
    private String description;
    private Boolean success;

    ResultCode(int code, Boolean success, String description) {
        this.code = code;
        this.success = success;
        this.description = description;
    }

    public static ResultCode get(Integer code) {
        for (ResultCode item : ResultCode.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }

    public static ResultCode get(String description) {
        for (ResultCode item : ResultCode.values()) {
            if (item.getDescription().equals(description)) {
                return item;
            }
        }
        return null;
    }
}