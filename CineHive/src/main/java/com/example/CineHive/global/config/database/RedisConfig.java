package com.example.CineHive.global.config.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    /**
     * Redis 서버와의 연결을 생성하는 RedisConnectionFactory Bean을 설정합니다.
     * application.yml에 정의된 host와 port를 사용합니다.
     *
     * @return Lettuce 기반의 RedisConnectionFactory 객체
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Redis 작업을 수행하기 위한 RedisTemplate Bean을 설정합니다.
     * Key와 Value의 Serializer를 StringRedisSerializer로 설정하여,
     * Redis에 저장될 때와 조회될 때 문자열로 올바르게 변환되도록 합니다.
     *
     * @return 설정이 완료된 RedisTemplate 객체
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Key Serializer는 String으로 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Value Serializer도 String으로 설정 (객체를 저장하려면 Jackson2JsonRedisSerializer 등을 사용)
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}