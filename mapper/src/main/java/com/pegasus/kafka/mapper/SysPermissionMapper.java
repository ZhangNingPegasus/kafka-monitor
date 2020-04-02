package com.pegasus.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pegasus.kafka.entity.dto.SysPage;
import com.pegasus.kafka.entity.dto.SysPermission;
import com.pegasus.kafka.entity.vo.PageVo;
import com.pegasus.kafka.entity.vo.PermissionVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * The mapper for role's permission.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Repository
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @MapKey("id")
    Map<Long, PageVo> getPermission(@Nullable @Param("sysAdminId") Long sysAdminId);

    List<PermissionVo> list(IPage<PermissionVo> page,
                            @Nullable @Param("sysRoleId") Long sysRoleId,
                            @Nullable @Param("sysPageId") Long sysPageId);

    List<SysPage> getPermissionPagesByRoleId(@Param("sysRoleId") Long sysRoleId);
}