package com.pegasus.kafka.common.exception;

import com.pegasus.kafka.common.response.ResultCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String description;

    public BusinessException(Exception e) {
        super(e);
        this.code = 0;
        this.description = e.getMessage();
    }

    public BusinessException(String description) {
        super(description);
        this.code = 0;
        this.description = description;
    }

    private BusinessException(Integer code, String description) {
        super(description);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getDescription());
    }


    public int getCode() {
        return code;
    }


    public void setCode(int code) {
        this.code = code;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}