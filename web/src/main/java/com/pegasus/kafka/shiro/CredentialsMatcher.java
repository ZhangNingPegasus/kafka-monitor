package com.pegasus.kafka.shiro;

import com.pegasus.kafka.entity.vo.AdminVo;
import com.pegasus.kafka.service.dto.SysPageService;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The credential matcher for shiro
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
public class CredentialsMatcher extends SimpleCredentialsMatcher {

    @Autowired
    private SysPageService sysPageService;

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        AdminVo appVo = (AdminVo) info.getPrincipals().getPrimaryPrincipal();
        sysPageService.fillPages(appVo);
        return true;
    }
}
