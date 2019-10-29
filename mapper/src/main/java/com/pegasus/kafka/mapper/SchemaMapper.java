package com.pegasus.kafka.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pegasus.kafka.entity.dto.TopicRecord;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemaMapper extends BaseMapper<TopicRecord> {

    void createDatabaseIfNotExists();

    void createTableIfNotExists();
}
