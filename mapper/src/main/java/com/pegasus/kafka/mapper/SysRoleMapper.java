package com.pegasus.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pegasus.kafka.entity.dto.SysRole;
import org.springframework.stereotype.Repository;

/**
 * The mapper for sys_role's schema. Using for administrator's role.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Repository
public interface SysRoleMapper extends BaseMapper<SysRole> {

}
