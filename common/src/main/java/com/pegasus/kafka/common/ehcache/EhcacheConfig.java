package com.pegasus.kafka.common.ehcache;

import com.pegasus.kafka.common.constant.Constants;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * the config for ehcache.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Component
public class EhcacheConfig {
    public static final Integer IDLE_SECONDS = 60;
    public static final Integer LIVE_SECONDS = 60;

    @Bean
    public CacheManager cacheManager() {
        CacheConfiguration config = new CacheConfiguration();
        config.setName(Constants.EHCACHE_CONFIG_NAME);
        config.setMaxEntriesLocalHeap(0);
        config.setEternal(false);
        config.setMemoryStoreEvictionPolicy("LFU");
        config.setTimeToIdleSeconds(IDLE_SECONDS);
        config.setTimeToLiveSeconds(LIVE_SECONDS);

        Configuration configuration = new Configuration();
        configuration.addCache(config);
        return CacheManager.create(configuration);
    }
}
