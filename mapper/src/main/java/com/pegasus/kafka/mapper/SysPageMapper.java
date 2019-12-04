package com.pegasus.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pegasus.kafka.entity.dto.SysPage;
import com.pegasus.kafka.entity.vo.PageInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The mapper for database's schema. Using for administrator's information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Repository
public interface SysPageMapper extends BaseMapper<SysPage> {
    List<PageInfo> list(IPage page, @Param("name") String name);

    Long getMaxOrderNum(@Param("parentId") Long parentId);
}
