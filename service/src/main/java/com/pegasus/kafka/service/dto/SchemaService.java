package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.TopicRecord;
import com.pegasus.kafka.mapper.SchemaMapper;
import org.springframework.stereotype.Service;

@Service
public class SchemaService extends ServiceImpl<SchemaMapper, TopicRecord> {

    @TranSave
    public void initSchema() {
        createDatabaseIfNotExists();
        createTableIfNotExists();
    }

    private void createDatabaseIfNotExists() {
        this.baseMapper.createDatabaseIfNotExists();
    }

    private void createTableIfNotExists() {
        this.baseMapper.createTableIfNotExists();
    }

}
