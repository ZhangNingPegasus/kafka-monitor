package com.pegasus.kafka.interceptor;

import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.entity.dto.SysAdmin;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The interceptor for login
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            try {
                SysAdmin sysAdmin = (SysAdmin) SecurityUtils.getSubject().getPrincipal();
                if (sysAdmin != null) {
                    request.setAttribute(Constants.CURRENT_ADMIN_LOGIN, sysAdmin);
                }
            } catch (Exception e) {

            }
        }
        return super.preHandle(request, response, handler);
    }
}
