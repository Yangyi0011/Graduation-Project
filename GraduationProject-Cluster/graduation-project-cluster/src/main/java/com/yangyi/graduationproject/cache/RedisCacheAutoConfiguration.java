package com.yangyi.graduationproject.cache;

/**
 * Created by IntelliJ IDEA.
 * User: YangYi
 * Date: 2019/4/24
 * Time: 15:12
 */

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.Duration;


@EnableCaching //启用SpringCache缓存
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisCacheAutoConfiguration extends CachingConfigurerSupport {
    //设置缓存存在时间为 30 分钟
    private static final Duration ttl = Duration.ofSeconds(30*60);

    /**
     * 自定义缓存key的生成策略。默认的生成策略是看不懂的(乱码内容) ，
     * 通过Spring 的依赖注入特性进行自定义的配置注入并且此类是一个配置类可以更多程度的自定义配置
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

    /**
     * 缓存配置管理器
     */
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory factory) {
        //以锁写入的方式创建RedisCacheWriter对象
        RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(factory);
        /*
            设置CacheManager的Value序列化方式为JdkSerializationRedisSerializer,
            但其实RedisCacheConfiguration默认就是使用
            StringRedisSerializer序列化key，
            JdkSerializationRedisSerializer序列化value,
            所以以下注释代码就是默认实现，没必要写，直接注释掉
         */
        // RedisSerializationContext.SerializationPair pair = RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer(this.getClass().getClassLoader()));
        // RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair);

        //创建默认缓存配置对象，并添加缓存失效时间
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(ttl);
        return new RedisCacheManager(writer, config);
    }

    /**
     * 自定义Template
     * 默认情况下的模板只能支持RedisTemplate<String,String>，只能存字符串。
     * 这时需要自定义模板，当自定义模板后又想存储String字符串时，
     *  可以使用StringRedisTemplate的方式，他们俩并不冲突。
     *
     *  spring-data-redis中序列化类有以下几个：
             GenericToStringSerializer：可以将任何对象泛化为字符创并序列化
             Jackson2JsonRedisSerializer：序列化Object对象为json字符创（与JacksonJsonRedisSerializer相同）
             JdkSerializationRedisSerializer：序列化java对象
             StringRedisSerializer：简单的字符串序列化
     */
    @Bean
    public RedisTemplate<String,Serializable> redisCacheTemplate(LettuceConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Serializable> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}