package com.yangyi.graduationproject.controller;

/**
 * Created by IntelliJ IDEA.
 * User: YangYi
 * Date: 2019/4/24
 * Time: 15:14
 */

import com.yangyi.graduationproject.entities.NewsInfo;
import com.yangyi.graduationproject.service.NewsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 redis其他类型的对应操作方式：
     opsForValue：对应String字符串
 　　opsForZSet：对应ZSet有序集合
 　　opsForHash：对应Hash哈希
 　　opsForList：对应List列表
 　　opsForSet：对应Set集合
 　　opsForGeo：对应GEO地理位置
 */
@RequestMapping("/redis")
@RestController
public class RedisTestController {
    @Qualifier("redisCacheTemplate")
    @Autowired
    private RedisTemplate redisCacheTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Qualifier("newsInfoService")
    @Autowired
    private NewsInfoService newsInfoService;

    @GetMapping("/test/{id}")
    public NewsInfo redis(@PathVariable("id") Integer id){

        //字符串，缓存10分钟
        stringRedisTemplate.opsForValue().set("rediskey","redisvalue",10, TimeUnit.MINUTES);
        String rediskey = stringRedisTemplate.opsForValue().get("rediskey");
        System.out.println(rediskey);

        //对象
        NewsInfo newsInfo = newsInfoService.getNewsInfoById(id);
        String key = "newsInfo_"+newsInfo.getId();
        //缓存10分钟
        redisCacheTemplate.opsForValue().set(key,newsInfo,10, TimeUnit.MINUTES);

        NewsInfo info = (NewsInfo) redisCacheTemplate.opsForValue().get(key);
        System.out.println("获取："+info);
        return newsInfo;
    }
}