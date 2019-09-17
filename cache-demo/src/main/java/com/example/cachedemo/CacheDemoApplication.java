package com.example.cachedemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * https://www.cnblogs.com/yueshutong/p/9381540.html
 */
@SpringBootApplication
public class CacheDemoApplication implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(CacheDemoApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {

    }
}

@Data
@AllArgsConstructor
class Member implements Serializable {
    Integer id;
    String username;
    String password;
    String info;
    Date createAt;

    public Member(Integer id, String username, String password, String info) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.info = info;
        this.createAt = new Date();
    }
}

@Service
@EnableCaching
class MemberService{
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 有缓存读缓存
     * @param id
     * @return
     */
    @Cacheable(cacheNames = "myCache", key = "'member_'.concat(#id)")
    public Member getMemberById(Integer id){
        logger.info("未使用缓存 '{}'!", id);
        return new Member( id, "freewolf", "1234456", "admin" );
    }

    /**
     * 删除某缓存
     * @param id
     */
    @CacheEvict(cacheNames = "myCache", key = "'member_'.concat(#id)")
    public void clear(Integer id){
        logger.info("清除缓存 '{}'!", id);
    }

    @CachePut(cacheNames = "myCache", key = "'member_'.concat(#id)")
    public Member update(Integer id){
        logger.info("更新缓存 '{}'!", id);
        return new Member( id, "freewolf", "1234456", "admin" );
    }

}

@RestController
class MemberController{
    @Autowired
    MemberService memberService;

    @GetMapping("/member/{id}")
    public Member getById(@PathVariable Integer id){
        return this.memberService.getMemberById(id);
    }

    @GetMapping("/clear/{id}")
    public String clearById(@PathVariable Integer id){
        this.memberService.clear(id);
        return "ok";
    }

    @GetMapping("/update/{id}")
    public Member updateById(@PathVariable Integer id){
        return this.memberService.update(id);
    }
}

@ConfigurationProperties(prefix = "cache")
@Data
class CacheConfigurationProperties {

    private long timeoutSeconds = 60;
    private int redisPort = 6379;
    private String redisHost = "localhost";
    // Mapping of cacheNames to expira-after-write timeout in seconds
    private Map<String, Long> cacheExpirations = new HashMap<>();
}

@Configuration
@EnableConfigurationProperties(CacheConfigurationProperties.class)
@Slf4j
class CacheConfig extends CachingConfigurerSupport {

    private static RedisCacheConfiguration createCacheConfiguration(long timeoutInSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(timeoutInSeconds));
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(CacheConfigurationProperties properties) {
        log.info("Redis (/Lettuce) configuration enabled. With cache timeout " + properties.getTimeoutSeconds() + " seconds.");

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(properties.getRedisHost());
        redisStandaloneConfiguration.setPort(properties.getRedisPort());
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(cf);
        return redisTemplate;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration(CacheConfigurationProperties properties) {
        return createCacheConfiguration(properties.getTimeoutSeconds());
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, CacheConfigurationProperties properties) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        for (Map.Entry<String, Long> cacheNameAndTimeout : properties.getCacheExpirations().entrySet()) {
            cacheConfigurations.put(cacheNameAndTimeout.getKey(), createCacheConfiguration(cacheNameAndTimeout.getValue()));
        }

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration(properties))
                .withInitialCacheConfigurations(cacheConfigurations).build();
    }
}