package com.pegasus.kafka.shiro;

import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysAdmin;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;

/**
 * The credential matcher for shiro
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
public class CredentialsMatcher extends SimpleCredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        SysAdmin sysAdmin = (SysAdmin) info.getPrincipals().getPrimaryPrincipal();
        String password = Common.hash(new String(usernamePasswordToken.getPassword()));
        return password.equals(sysAdmin.getPassword());
    }
}
