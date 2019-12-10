package com.pegasus.kafka.entity.echarts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

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
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class BarInfo implements Serializable {
    private List<String> yData;
    private List<Long> series;
    private String strYData;
    private String strSeries;
}
