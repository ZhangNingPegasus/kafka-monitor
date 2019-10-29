package com.pegasus.kafka.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pegasus.kafka.entity.dto.TopicRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface TopicRecordMapper extends BaseMapper<TopicRecord> {
    void createTableIfNotExists(@Param(value = "tableNames") Set<String> tableNames);

    void batchSave(@Param(value = "tableName") String tableName,
                   @Param(value = "topicRecords") List<TopicRecord> topicRecords);

    List<TopicRecord> listMessages(IPage page,
                                   @Param(value = "tableName") String tableName,
                                   @Param(value = "partitionId") Integer partitionId,
                                   @Param(value = "key") String key,
                                   @Param(value = "from") Date from,
                                   @Param(value = "to") Date to);

    TopicRecord findMessage(@Param(value = "tableName") String tableName,
                            @Param(value = "partitionId") Integer partitionId,
                            @Param(value = "offset") Long offset,
                            @Param(value = "key") String key
    );

    void dropTable(@Param(value = "tableName") String tableName);
}
