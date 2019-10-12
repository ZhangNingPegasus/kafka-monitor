package com.pegasus.kafka.common.exception;

import com.pegasus.kafka.common.response.ResultCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code = null;
    private String description = null;

    public BusinessException() {
        super();
    }

    public BusinessException(Throwable t) {
        super(t);
    }

    public BusinessException(String description) {
        super(description);
        this.code = 0;
        this.description = description;
    }

    public BusinessException(Integer code, String description) {
        super(description);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getDescription());
    }

    /**
     * 错误码
     *
     * @return
     */
    public int getCode() {
        return code;
    }

    /**
     * 错误码
     *
     * @param code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 错误描述
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * 错误描述
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}