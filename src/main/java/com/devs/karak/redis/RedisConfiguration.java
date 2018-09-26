package com.devs.karak.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class RedisConfiguration {

    private static final String KARAK_COUNTER = "KarakCounter";
    @Autowired
    private RedisTemplate redisTemplate;


    @Bean
    public RedisAtomicInteger createCounter(){
        RedisAtomicInteger counter = new RedisAtomicInteger(KARAK_COUNTER,redisTemplate.getConnectionFactory());
        return counter;
    }


}
