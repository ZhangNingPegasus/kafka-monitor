package com.pegasus.kafka.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.entity.dto.TopicRecordValue;
import com.pegasus.kafka.entity.po.MaxOffset;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * The mapper for dynamic table. Using for save the topics'content.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Repository
public interface TopicRecordMapper extends BaseMapper<TopicRecord> {
    void createTableIfNotExists(@Param(value = "tableName") String tableName);

    void createRecordTableIfNotExists(@Param(value = "recordTableName") String recordTableName);

    void batchSave(@Param(value = "tableName") String tableName,
                   @Param(value = "topicRecords") List<TopicRecord> topicRecords);

    void batchSaveRecord(@Param(value = "recordTableName") String recordTableName,
                         @Param(value = "topicRecordValues") List<TopicRecordValue> topicRecordValues);

    List<TopicRecord> listRecords(IPage page,
                                  @Param(value = "tableName") String tableName,
                                  @Param(value = "partitionId") Integer partitionId,
                                  @Param(value = "key") String key,
                                  @Param(value = "from") Date from,
                                  @Param(value = "to") Date to);

    TopicRecord findRecord(@Param(value = "tableName") String tableName,
                           @Param(value = "partitionId") Integer partitionId,
                           @Param(value = "offset") Long offset,
                           @Param(value = "key") String key
    );

    String findRecordValue(@Param(value = "tableName") String tableName,
                           @Param(value = "partitionId") Integer partitionId,
                           @Param(value = "offset") Long offset);

    void dropTable(@Param(value = "tableName") String tableName);


    Long getRecordsCount(@Param(value = "tableName") String tableName, @Param(value = "from") Date from, @Param(value = "to") Date to);

    List<MaxOffset> getMaxOffset(@Param(value = "tableName") String tableName);
}
