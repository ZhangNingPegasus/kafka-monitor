package com.pegasus.kafka.entity.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * The topic information
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Data
public class Topic implements Serializable {
    /**
     * the prefix of topic's name
     */
    private String name;

    /**
     * the topic names which need to subscibed
     */
    private List<String> topicNameList;
}
