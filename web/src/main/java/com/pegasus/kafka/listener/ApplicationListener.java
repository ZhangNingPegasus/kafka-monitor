package com.pegasus.kafka.listener;


import com.pegasus.kafka.service.dto.SchemaService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApplicationListener implements ApplicationContextAware {

    private final SchemaService schemaService;

    public ApplicationListener(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Thread schema = new Thread(new SchemaTask(), String.format("SCHEMA_INIT_%s", UUID.randomUUID()));
        schema.setDaemon(false);
        schema.start();
    }

    private class SchemaTask implements Runnable {
        @Override
        public void run() {
            schemaService.initSchema();
        }
    }
}
