package io.github.songminkyu.account.config;

import static io.github.songminkyu.account.repository.AccountRepository.ACCOUNT_BY_CUSTOMER_ID_CACHE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.cfg.AvailableSettings;
import org.redisson.api.RedissonClient;
import org.redisson.hibernate.RedissonRegionFactory;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@ConditionalOnClass({RedissonClient.class})
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    private final CacheProperties cacheProperties;
    private static final String ENTRY_REGION_NAME = "entry";

    @Bean
    @ConditionalOnProperty("spring.jpa.properties.hibernate.cache.use_second_level_cache")
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(RedissonClient redissonClient) {
        return hibernateProperties ->
            hibernateProperties.put(AvailableSettings.CACHE_REGION_FACTORY, new RedissonRegionFactory() {
                @Override
                protected RedissonClient createRedissonClient(StandardServiceRegistry registry, Map properties) {
                    return redissonClient;
                }
            });
    }

    @Bean
    public CacheManager cacheManager(
        RedissonClient redissonClient) {
        Map<String, org.redisson.spring.cache.CacheConfig> config = new HashMap<>();
        createCache(config,  ACCOUNT_BY_CUSTOMER_ID_CACHE);
        return new RedissonSpringCacheManager(redissonClient, config);
    }

    private void createCache(
        Map<String, org.redisson.spring.cache.CacheConfig> cacheConfigMap,
        String cacheName) {
        var regions = cacheProperties.getRedisson().getRegions();
        var region = Optional.ofNullable(regions.get(cacheName))
                .orElse(regions.get(ENTRY_REGION_NAME));
        var expiration = region.getExpiration();
        var cacheConfig = new org.redisson.spring.cache.CacheConfig(
            expiration.getTimeToLive(), expiration.getMaxIdleTime());
        cacheConfig.setMaxSize(expiration.getMaxEntries());
        cacheConfigMap.put(cacheName, cacheConfig);
    }
}
