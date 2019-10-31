package com.pegasus.kafka.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pegasus.kafka.entity.dto.SysLogSize;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

@Repository
public interface SysLogSizeMapper extends BaseMapper<SysLogSize> {

    List<SysLogSize> getTopicRank(@Param(value = "rank") Integer rank,
                                  @Nullable @Param(value = "from") Date from,
                                  @Nullable @Param(value = "to") Date to);

    Long getHistoryLogSize(@Param(value = "from") Date from, @Param(value = "to") Date to);
}
