package com.pegasus.kafka.advice;

import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * As the base class of the interface controller. Provide common functions such as unified error handling.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         1/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handleOtherException(Model model,
                                       HttpServletRequest request,
                                       Exception e) {
        return handleException(model, request, e, true);
    }

    private Object handleException(Model model,
                                   HttpServletRequest request,
                                   Exception exception,
                                   boolean needLog) {
        String errorMsg = getErrorMessage(exception);
        errorMsg = StringUtils.isEmpty(errorMsg) ? exception.getMessage() : errorMsg;
        if (needLog) {
            log.error(errorMsg, exception);
        }
        if (isAjax(request)) {
            ResultCode respondCode = ResultCode.get(errorMsg);
            if (respondCode == null) {
                return Result.error(errorMsg);
            } else {
                return Result.create(respondCode);
            }
        } else {
            model.addAttribute("error", getStackTrace(exception));
            return new ModelAndView("error/500");
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private String getErrorMessage(Throwable e) {
        if (e.getCause() != null) {
            return getErrorMessage(e.getCause());
        }
        return e.getMessage();
    }

    private String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        exception.printStackTrace(writer);
        return stringWriter.getBuffer().toString().replaceAll("\\r\\n\\t", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    }
}