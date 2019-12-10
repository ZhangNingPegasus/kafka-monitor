package com.pegasus.kafka.template;

import com.pegasus.kafka.entity.vo.AdminVo;
import com.pegasus.kafka.entity.vo.PageVo;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public abstract class AuthDirective {

    @Autowired
    protected HttpServletRequest request;

    protected boolean checkPermission(Operation operation) {

        if (operation == null) {
            return false;
        }

        String uri = request.getRequestURI();

        switch (operation) {
            case INSERT:
                return checkPermission(uri, PageVo::getCanInsert);
            case DELETE:
                return checkPermission(uri, PageVo::getCanDelete);
            case UPDATE:
                return checkPermission(uri, PageVo::getCanUpdate);
            case SELECT:
                return checkPermission(uri, (permission) -> permission.getCanDelete() || permission.getCanInsert() || permission.getCanUpdate()
                        || permission.getCanSelect());
        }
        return false;
    }

    private boolean checkPermission(String uri, HandlePermission handlePermission) {
        AdminVo adminVo = (AdminVo) SecurityUtils.getSubject().getPrincipal();
        if (adminVo == null) {
            return false;
        } else if (adminVo.getSysRole().getSuperAdmin()) {
            return true;
        } else if (adminVo.getPermissions() == null || adminVo.getPermissions().size() < 1) {
            return false;
        }

        if (uri == null) {
            uri = "";
        }

        PageVo pageVo = getByUri(adminVo.getPermissions(), uri);
        if (pageVo != null) {
            return handlePermission.check(pageVo);
        }

        return false;
    }

    private PageVo getByUri(List<PageVo> pageVoList, String uri) {
        for (PageVo pageVo : pageVoList) {
            if (pageVo.getUrl().equals(uri)) {
                return pageVo;
            } else if (pageVo.getChildren() != null && pageVo.getChildren().size() > 0) {
                return getByUri(pageVo.getChildren(), uri);
            }
        }
        return null;
    }

    protected enum Operation {
        INSERT,
        DELETE,
        UPDATE,
        SELECT
    }

    private interface HandlePermission {
        boolean check(PageVo pageVo);
    }

}