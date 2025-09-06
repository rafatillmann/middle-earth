package org.example.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;


public class CacheTTL<Long, String> {

    private final Cache<Long, String> cache;

    public CacheTTL(long ttl, TimeUnit unit) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(ttl, unit)
                .build();
    }

    public void put(Long key, String value) {
        cache.put(key, value);
    }

    public String getIfPresent(Long key) {
        return cache.getIfPresent(key);
    }

    public void invalidate(Long key) {
        cache.invalidate(key);
    }
}