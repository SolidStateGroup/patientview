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
 * Configuration for cache management using ehcache, typically used for service method calls that happen every request.
 * Created by jamesr@solidstategroup.com
 * Created on 08/10/2014
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final int MAX_ENTRIES_LOCAL_HEAP = 10000;

    @Bean(destroyMethod = "shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {

        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.defaultCache(defaultCache());

        // unread conversation count, simple repository call, evicted on loading conversations for user
        config.addCache(createCache("unreadConversationCount"));

        // authenticate based on token string, returning UserToken and storing group roles in granted authorities,
        // evicted on log out / log in / get user information
        config.addCache(createCache("authenticateOnToken"));

        // public group, used in my conditions and join requests
        config.addCache(createCache("findAllPublic"));

        // get monthly statistics, shown on dashboard
        config.addCache(createCache("getMonthlyGroupStatistics"));

        // news by user id, shown on dashboard, news pages
        config.addCache(createCache("findNewsByUserId"));

        // lookups, used when dealing with Code types
        config.addCache(createCache("findLookupByTypeAndValue"));

        // find Codes, used when retrieving patients in ApiPatientService.get()
        config.addCache(createCache("findAllByCodeAndType"));

        // find feature lookups, used during login
        config.addCache(createCache("getFeaturesByType"));

        // find lookup by type and value, used by front end and news
        config.addCache(createCache("getLookupByTypeAndValue"));

        // find lookup by type, used by front end
        config.addCache(createCache("getLookupsByType"));

        // find all lookups, used by front end
        config.addCache(createCache("getAllLookups"));

        // find all feature lookups, used by front end
        config.addCache(createCache("getAllFeatures"));

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
        return new CacheConfiguration(name, MAX_ENTRIES_LOCAL_HEAP);
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
