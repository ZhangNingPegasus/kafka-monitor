package com.pegasus.kafka.listener;


import com.pegasus.kafka.service.dto.SchemaService;
import com.pegasus.kafka.service.property.PropertyService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ExecutorService executorService;
    private final PropertyService propertyService;

    public ApplicationListener(SchemaService schemaService,
                               PropertyService propertyService) {
        this.schemaService = schemaService;
        this.propertyService = propertyService;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!propertyService.getNeedInitial()) {
            return;
        }
        executorService.submit(new SchemaTask());
    }

    private class SchemaTask implements Runnable {
        @Override
        public void run() {
            schemaService.createTableIfNotExists();
        }
    }
}