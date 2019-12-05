package com.pegasus.kafka.template;

import com.pegasus.kafka.entity.vo.AdminInfo;
import com.pegasus.kafka.entity.vo.PageInfo;
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
                return checkPermission(uri, PageInfo::getCanInsert);
            case DELETE:
                return checkPermission(uri, PageInfo::getCanDelete);
            case UPDATE:
                return checkPermission(uri, PageInfo::getCanUpdate);
            case SELECT:
                return checkPermission(uri, (permission) -> permission.getCanDelete() || permission.getCanInsert() || permission.getCanUpdate()
                        || permission.getCanSelect());
        }
        return false;
    }

    private boolean checkPermission(String uri, HandlePermission handlePermission) {
        AdminInfo adminInfo = (AdminInfo) SecurityUtils.getSubject().getPrincipal();
        if (adminInfo == null) {
            return false;
        } else if (adminInfo.getSysRole().getSuperAdmin()) {
            return true;
        } else if (adminInfo.getPermissions() == null || adminInfo.getPermissions().size() < 1) {
            return false;
        }

        if (uri == null) {
            uri = "";
        }

        PageInfo pageInfo = getByUri(adminInfo.getPermissions(), uri);
        if (pageInfo != null) {
            return handlePermission.check(pageInfo);
        }

        return false;
    }

    private PageInfo getByUri(List<PageInfo> pageInfoList, String uri) {
        for (PageInfo pageInfo : pageInfoList) {
            if (pageInfo.getUrl().equals(uri)) {
                return pageInfo;
            } else if (pageInfo.getChildren() != null && pageInfo.getChildren().size() > 0) {
                return getByUri(pageInfo.getChildren(), uri);
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
        boolean check(PageInfo pageInfo);
    }

}