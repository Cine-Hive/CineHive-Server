package com.example.CineHive.entity.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * Refresh Token을 Redis에 저장하기 위한 엔티티 클래스입니다.
 *
 * @RedisHash: Redis의 Key-Value 구조에서 Key의 Prefix(keyspace)를 정의합니다.
 * 여기서는 "refreshToken"이라는 Prefix가 사용되어, Redis에는 "refreshToken:{id}" 형태로 Key가 저장됩니다.
 * @TimeToLive: Redis 데이터의 만료 시간(TTL)을 설정합니다.
 * 지정된 시간이 지나면 데이터는 자동으로 삭제됩니다.
 */
@Getter
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id
    private final String email;

    @Indexed
    private final String token;

    @TimeToLive
    private final Long expiration;

    /**
     * RefreshToken 객체를 생성합니다.
     * @param email 사용자의 이메일 (Redis Key)
     * @param token 발급된 Refresh Token
     * @param expiration 토큰 만료 시간 (초 단위)
     */
    public RefreshToken(String email, String token, Long expiration) {
        this.email = email;
        this.token = token;
        this.expiration = expiration;
    }
}

