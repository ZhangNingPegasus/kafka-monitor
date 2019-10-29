package com.pegasus.kafka.common.ehcache;

import com.pegasus.kafka.common.constant.Constants;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class EhcacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CacheConfiguration config = new CacheConfiguration();
        config.setName(Constants.EHCACHE_CONFIG_NAME);
        config.setMaxEntriesLocalHeap(2000);
        config.setEternal(false);//是否永不过期，false则过期, 需要通过timeToIdleSeconds，timeToLiveSeconds判断
        config.setMemoryStoreEvictionPolicy("LFU");//最少使用
        config.setTimeToIdleSeconds(30);
        config.setTimeToLiveSeconds(30);

        // 设置ehcache配置文件，获取CacheManager
        Configuration configuration = new Configuration();
        configuration.addCache(config);
        return CacheManager.create(configuration);
    }
}
