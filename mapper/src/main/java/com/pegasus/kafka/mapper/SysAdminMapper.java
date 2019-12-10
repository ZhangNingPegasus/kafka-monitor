package com.pegasus.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pegasus.kafka.entity.dto.SysAdmin;
import com.pegasus.kafka.entity.vo.AdminVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * The mapper for database's schema. Using for administrator's information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Repository
public interface SysAdminMapper extends BaseMapper<SysAdmin> {

    IPage<AdminVo> list(Page page, @Param("name") String name);

    AdminVo getById(@Param("sysAdminId") Long sysAdminId);

    AdminVo getByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}
