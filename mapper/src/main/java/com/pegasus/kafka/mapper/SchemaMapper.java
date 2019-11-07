package com.pegasus.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pegasus.kafka.entity.dto.TopicRecord;
import org.springframework.stereotype.Repository;

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

    void createDatabaseIfNotExists();

    void createTableIfNotExists();
}
