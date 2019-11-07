package com.pegasus.kafka.listener;


import com.pegasus.kafka.service.dto.SchemaService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * The listener for SpringMVC. Used for initiate the database and table's schema.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
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
