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
@AllArgsConstructor
@RedisHash(value = "refreshToken")
public class RefreshToken {

    /**
     * Refresh Token의 Key가 될 사용자의 이메일입니다.
     */
    @Id
    private String email;

    /**
     * 실제 Refresh Token 문자열입니다.
     * @Indexed 어노테이션을 통해 이 필드로 데이터를 검색할 수 있습니다. (FindByToken)
     */
    @Indexed
    private String token;

    /**
     * 토큰의 만료 시간(초 단위)입니다.
     * 이 필드에 설정된 시간이 지나면 Redis에서 자동으로 삭제됩니다.
     */
    @TimeToLive
    private Long expiration;
}
