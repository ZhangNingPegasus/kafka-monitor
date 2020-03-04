package com.pegasus.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pegasus.kafka.entity.dto.TopicRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Set;

/**
 * The mapper for database's schema. Using for create database and related tables in the first running time.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Repository
public interface SchemaMapper extends BaseMapper<TopicRecord> {
    void createTableIfNotExists(@Param("dbName") String dbName);

    void deleteExpired(@Param("dbName") String dbName,
                       @Param("tableNameList") Set<String> tableNameList,
                       @Param("dateTime") Date dateTime);

    Set<String> listTables(@Param("dbName") String dbName);
}
