package com.pegasus.kafka.config;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.sijibao.nacos.spring.context.annotation.EnableNacosExConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableNacosExConfig
@NacosPropertySource(dataId = "kafka.monitor.info.set", groupId = "GLOBAL_SIJIBAO_BASIC_GROUP")
public class NacosSpringConfig {
}