package com.example.CineHive.domain.auth.repository;

import com.example.CineHive.domain.auth.entity.RefreshToken;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Redis에 저장된 RefreshToken에 접근하기 위한 Repository 인터페이스입니다.
 * Spring Data Redis는 이 인터페이스의 구현체를 자동으로 생성해줍니다.
 * Key 타입은 RefreshToken의 ID 타입인 String (email) 입니다.
 */
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    /**
     * Refresh Token 문자열을 기준으로 RefreshToken 객체를 조회합니다.
     * 토큰 재발급 요청 시, 전달받은 토큰이 실제로 Redis에 존재하는지 검증하는 데 사용됩니다.
     *
     * @param token 조회할 Refresh Token
     * @return Optional<RefreshToken> 객체
     */
    Optional<RefreshToken> findByToken(String token);
}
