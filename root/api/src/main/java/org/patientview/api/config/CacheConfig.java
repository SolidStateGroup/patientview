package org.patientview.api.config;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/10/2014
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean(destroyMethod="shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {

        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.defaultCache(defaultCache());

        // unread conversation count, simple repository call, evicted on loading conversations for user
        config.addCache(createCache("unreadConversationCount"));

        // authenticate based on token string, returning UserToken and storing group roles in granted authorities,
        // evicted on log out / log in / get user information
        config.addCache(createCache("authenticateOnToken"));

        return net.sf.ehcache.CacheManager.newInstance(config);
    }

    /*
      This is the cache 'template' that EhCache will use when creating other caches
     */
    private CacheConfiguration defaultCache() {
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("patientview-default");
        cacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
        cacheConfiguration.setEternal(true);
        return cacheConfiguration;
    }

    private CacheConfiguration createCache(String name) {
        return new CacheConfiguration(name, 10000);
    }

    @Bean
    @Override
    public org.springframework.cache.CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }
}
