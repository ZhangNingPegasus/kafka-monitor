package com.pegasus.kafka.common.ehcache;

import com.pegasus.kafka.common.constant.Constants;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class EhcacheService {
    // 默认的缓存存在时间（秒）
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
                0,// timeToIdleSeconds
                DEFAULT_LIVE_SECOND);
        cache.put(element);
    }


    public <T> T get(String key, Class<T> cls) {
        Cache cache = cacheManager.getCache(Constants.EHCACHE_CONFIG_NAME);
        Element element = cache.get(key);
        if (element == null) {
            return null;
        }
        Object value = element.getObjectValue();
        return (T) value;
    }

    public void clear() {
        Cache cache = cacheManager.getCache(Constants.EHCACHE_CONFIG_NAME);
        cache.removeAll();
    }


}
