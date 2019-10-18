package com.pegasus.kafka.advice;

import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.common.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
public class ExceptionControllerAdvice {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    private static String getErrorMessage(Throwable e) {
        if (e.getCause() != null) {
            return getErrorMessage(e.getCause());
        }
        return e.getMessage();
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<?> handleBusinessException(Exception e) {
        return handleException(e, false);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<Integer> handleMethodNotSupportedException() {
        return ResponseEntity.badRequest().body(null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<?> handleOtherException(Exception e) {
        return handleException(e, true);
    }

    private Result<?> handleException(Exception e,
                                      boolean logger) {
        String errMsg = getErrorMessage(e);
        String strError = StringUtils.isEmpty(errMsg) ? e.getMessage() : errMsg;
        ResultCode respondCode = ResultCode.get(strError);
        if (logger) {
            LOGGER.error(strError, e);
            e.printStackTrace();
        }
        if (respondCode == null) {
            return Result.error(strError);
        } else {
            return Result.create(respondCode);
        }
    }
}
