package com.pegasus.kafka.config;

import com.pegasus.kafka.template.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FreeMarker's configuration
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Configuration
public class FreeMarkerConfig implements InitializingBean {
    private final freemarker.template.Configuration freeMarkerConfiguration;

    public FreeMarkerConfig(freemarker.template.Configuration freeMarkerConfiguration) {
        this.freeMarkerConfiguration = freeMarkerConfiguration;
    }

    @Bean
    public InsertDirective insertDirective() {
        return new InsertDirective();
    }

    @Bean
    public NoInsertDirective noInsertDirective() {
        return new NoInsertDirective();
    }

    @Bean
    public DeleteDirective deleteDirective() {
        return new DeleteDirective();
    }

    @Bean
    public NoDeleteDirective noDeleteDirective() {
        return new NoDeleteDirective();
    }

    @Bean
    public UpdateDirective updateDirective() {
        return new UpdateDirective();
    }

    @Bean
    public NoUpdateDirective noUpdateDirective() {
        return new NoUpdateDirective();
    }

    @Bean
    public SelectDirective selectDirective() {
        return new SelectDirective();
    }

    @Bean
    public NoSelectDirective noSelectDirective() {
        return new NoSelectDirective();
    }

    @Bean
    public OnlySelectDirective onlySelectDirective() {
        return new OnlySelectDirective();
    }

    @Bean
    public NotOnlySelectDirective notOnlySelectDirective() {
        return new NotOnlySelectDirective();
    }

    @Override
    public void afterPropertiesSet() {
        freeMarkerConfiguration.setSharedVariable("insert", insertDirective());
        freeMarkerConfiguration.setSharedVariable("delete", deleteDirective());
        freeMarkerConfiguration.setSharedVariable("update", updateDirective());
        freeMarkerConfiguration.setSharedVariable("select", selectDirective());

        freeMarkerConfiguration.setSharedVariable("no_insert", noInsertDirective());
        freeMarkerConfiguration.setSharedVariable("no_delete", noDeleteDirective());
        freeMarkerConfiguration.setSharedVariable("no_update", noUpdateDirective());
        freeMarkerConfiguration.setSharedVariable("no_select", noSelectDirective());

        freeMarkerConfiguration.setSharedVariable("only_select", onlySelectDirective());

        freeMarkerConfiguration.setSharedVariable("not_only_select", notOnlySelectDirective());
    }
}
