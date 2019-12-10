package com.pegasus.kafka.shiro;

import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.vo.AdminVo;
import com.pegasus.kafka.service.dto.SysAdminService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The authentication realm for shiro
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
public class AuthRealm extends AuthorizingRealm {
    @Autowired
    private SysAdminService sysAdminService;

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        AdminVo adminVo = sysAdminService.getByUsernameAndPassword(usernamePasswordToken.getUsername(), Common.hash(new String(usernamePasswordToken.getPassword())));
        if (adminVo == null) {
            return null;
        }

        return new SimpleAuthenticationInfo(adminVo, usernamePasswordToken.getPassword(), usernamePasswordToken.getUsername());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return new SimpleAuthorizationInfo();
    }
}
