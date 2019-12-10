package com.pegasus.kafka.entity.echarts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * The view object for cpu information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CpuInfo implements Serializable {
    private List<String> xAxis;
    private List<Double> systemCpu;
    private List<Double> processCpu;

    private String strXAxis;
    private String strSystemCpu;
    private String strProcessCpu;
}
