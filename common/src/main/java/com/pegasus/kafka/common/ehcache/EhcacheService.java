package com.pegasus.kafka.common.ehcache;

import com.pegasus.kafka.common.constant.Constants;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


/**
 * the service for ehcache.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Component
public class EhcacheService {
    private static final int DEFAULT_LIVE_SECOND = 30 * 60;
    @Lazy
    private final CacheManager cacheManager;

    public EhcacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void set(String key, Object value) {
        Cache cache = cacheManager.getCache(Constants.EHCACHE_CONFIG_NAME);
        Element element = new Element(
                key,
                value,
                0,
                DEFAULT_LIVE_SECOND);
        cache.put(element);
    }

    public void set(String key, Object value, Integer expiredInSeconds) {
        Cache cache = cacheManager.getCache(Constants.EHCACHE_CONFIG_NAME);
        Element element = new Element(
                key,
                value,
                expiredInSeconds,
                DEFAULT_LIVE_SECOND);
        cache.put(element);
    }


    public <T> T get(String key) {
        Cache cache = cacheManager.getCache(Constants.EHCACHE_CONFIG_NAME);
        Element element = cache.get(key);
        if (element == null) {
            return null;
        }
        Object value = element.getObjectValue();
        return (T) value;
    }

    public boolean remove(String key) {
        Cache cache = cacheManager.getCache(Constants.EHCACHE_CONFIG_NAME);
        return cache.remove(key);
    }

    public void clear() {
        Cache cache = cacheManager.getCache(Constants.EHCACHE_CONFIG_NAME);
        cache.removeAll();
    }


}
