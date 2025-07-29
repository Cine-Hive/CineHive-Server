package com.example.CineHive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
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
     * @param redisConnectionFactory Redis 연결을 위한 팩토리
     * @return 설정이 완료된 RedisTemplate 객체
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Key와 Value의 직렬화/역직렬화 방식을 String으로 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        // Hash Key와 Hash Value의 직렬화/역직렬화 방식도 String으로 설정 (필요 시)
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        return template;
    }
}
