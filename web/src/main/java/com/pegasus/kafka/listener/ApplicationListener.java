package com.pegasus.kafka.listener;


import com.pegasus.kafka.service.dto.SchemaService;
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
    private ExecutorService executorService;

    public ApplicationListener(SchemaService schemaService) {
        this.schemaService = schemaService;
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        executorService.submit(new SchemaTask());
    }

    private class SchemaTask implements Runnable {
        @Override
        public void run() {
            schemaService.createTableIfNotExists();
        }
    }
}
