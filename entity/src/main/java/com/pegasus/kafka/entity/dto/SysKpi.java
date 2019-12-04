package com.pegasus.kafka.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.constant.JMX;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * The entity for table sys_kpi. Using for saving zookeeper and kafka's kpi information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = Constants.DATABASE_NAME + "." + "`sys_kpi`")
public class SysKpi extends BaseDto {
    /**
     * kpi的主机信息
     */
    @TableField(value = "`host`")
    private String host;

    /**
     * kpi指标名称
     */
    @TableField(value = "`kpi`")
    private Integer kpi;

    /**
     * kpi值
     */
    @TableField(value = "`value`")
    private Double value;

    @Getter
    public enum ZK_KPI {
        ZK_PACKETS_RECEIVED(1, JMX.ZK_PACKETS_RECEIVED),
        ZK_PACKETS_SENT(2, JMX.ZK_PACKETS_SENT),
        ZK_NUM_ALIVE_CONNECTIONS(3, JMX.ZK_NUM_ALIVE_CONNECTIONS),
        ZK_OUTSTANDING_REQUESTS(4, JMX.ZK_OUTSTANDING_REQUESTS);

        private int code;
        private String name;

        ZK_KPI(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public static ZK_KPI get(Integer code) {
            for (ZK_KPI item : ZK_KPI.values()) {
                if (item.getCode() == code) {
                    return item;
                }
            }
            return null;
        }
    }

    @Getter
    public enum KAFKA_KPI {
        KAFKA_MESSAGES_IN(5, JMX.MESSAGES_IN),
        KAFKA_BYTES_IN(6, JMX.BYTES_IN),
        KAFKA_BYTES_OUT(7, JMX.BYTES_OUT),
        KAFKA_BYTES_REJECTED(8, JMX.BYTES_REJECTED),
        KAFKA_FAILED_FETCH_REQUEST(9, JMX.FAILED_FETCH_REQUEST),
        KAFKA_FAILED_PRODUCE_REQUEST(10, JMX.FAILED_PRODUCE_REQUEST),
        KAFKA_TOTAL_FETCH_REQUESTS_PER_SEC(11, JMX.TOTAL_FETCH_REQUESTS_PER_SEC),
        KAFKA_TOTAL_PRODUCE_REQUESTS_PER_SEC(12, JMX.TOTAL_PRODUCE_REQUESTS_PER_SEC),
        KAFKA_REPLICATION_BYTES_IN_PER_SEC(13, JMX.REPLICATION_BYTES_IN_PER_SEC),
        KAFKA_REPLICATION_BYTES_OUT_PER_SEC(14, JMX.REPLICATION_BYTES_OUT_PER_SEC),
        KAFKA_PRODUCE_MESSAGE_CONVERSIONS(15, JMX.PRODUCE_MESSAGE_CONVERSIONS),
        KAFKA_OS_TOTAL_MEMORY(16, JMX.OS_TOTAL_MEMORY),
        KAFKA_OS_FREE_MEMORY(17, JMX.OS_FREE_MEMORY),
        KAFKA_OS_USED_MEMORY_PERCENTAGE(18, "");

        private int code;
        private String name;

        KAFKA_KPI(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public static KAFKA_KPI get(Integer code) {
            for (KAFKA_KPI item : KAFKA_KPI.values()) {
                if (item.getCode() == code) {
                    return item;
                }
            }
            return null;
        }
    }
}
