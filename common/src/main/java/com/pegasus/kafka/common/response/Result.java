package com.pegasus.kafka.common.response;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * the uniformed response used for ajax.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer code;
    private String error;
    private Boolean ok;
    private Long count;
    private T data;

    private Result(int code, String error, T data, Boolean ok) {
        this.code = code;
        this.error = error;
        this.data = data;
        this.ok = ok;
    }

    public static <T> Result<T> create(ResultCode respondCode, T data) {
        return new Result<>(respondCode.getCode(), respondCode.getDescription(), data, respondCode.getSuccess());
    }

    public static <T> Result<T> create(ResultCode respondCode) {
        return new Result<>(respondCode.getCode(), respondCode.getDescription(), null, respondCode.getSuccess());
    }


    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), null, data, ResultCode.SUCCESS.getSuccess());
    }

    public static <T extends Collection> Result<T> success(T data, Long count) {
        Result<T> result = new Result<>(ResultCode.SUCCESS.getCode(), null, data, ResultCode.SUCCESS.getSuccess());
        result.setCount(count);
        return result;
    }

    public static <T extends Collection> Result<T> success(T data, Integer count) {
        Result<T> result = new Result<>(ResultCode.SUCCESS.getCode(), null, data, ResultCode.SUCCESS.getSuccess());
        result.setCount((long) count);
        return result;
    }

    public static <T> Result<List<T>> success(IPage<T> page) {
        Result<List<T>> result = new Result<>(ResultCode.SUCCESS.getCode(), null, page.getRecords(),
                ResultCode.SUCCESS.getSuccess());
        result.setCount(page.getTotal());
        return result;
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), null, null, ResultCode.SUCCESS.getSuccess());
    }

    public static <T> Result<T> error(T data) {
        return new Result<>(ResultCode.ERROR.getCode(), null, data, ResultCode.ERROR.getSuccess());
    }

    public static <T> Result<T> error(String error) {
        return new Result<>(ResultCode.ERROR.getCode(), error, null, ResultCode.ERROR.getSuccess());
    }

    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), null, null, ResultCode.ERROR.getSuccess());
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Boolean getOk() {
        return ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}