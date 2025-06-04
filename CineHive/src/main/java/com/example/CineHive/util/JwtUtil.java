package com.example.CineHive.util;

import com.example.CineHive.exception.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간

    public String generateToken(String email) {
        log.info("Generating token for email: {}", email);
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    /**
     * JWT 토큰을 실제로 생성하는 내부 메서드
     * 주어진 클레임(Claim)과 Subject(토큰 주체), 발급 시간, 만료 시간, 서명 정보를 포함하여 토큰을 빌드
     *
     * @param claims 토큰에 포함될 추가 클레임 (예: 사용자 역할, 권한 등)
     * @param subject 토큰의 주체 (예: 사용자 ID 또는 이메일)
     * @return 생성된 JWT 토큰 문자열
     * @throws Exception 토큰 생성 중 오류 발생 시 예외를 던짐
     */

    private String createToken(Map<String, Object> claims, String subject) {
        try {
            // secretKey를 Base64 디코딩 후 사용하여 SecretKey 생성
            byte[] decodedKey = Base64.getDecoder().decode(secretKey);
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(key)
                    .compact();

            log.info("Generated JWT: {}", token);
            return token;
        } catch (Exception e) {
            log.error("Error generating JWT: {}", e.getMessage(), e);
            throw e;
        }
    }
    /**
     * 주어진 JWT 토큰에서 Subject 클레임 (일반적으로 사용자 이메일이나 ID)을 추출하는 메서드
     * 내부적으로 extractAllClaims 메서드를 호출하여 모든 클레임을 가져온 후 Subject만 반환
     *
     * @param token Subject를 추출할 대상 JWT 토큰 문자열
     * @return 토큰의 Subject 클레임 (사용자 이메일 등)
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        // secretKey를 Base64 디코딩 후 사용하여 SecretKey 생성
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증하는 메서드
     * 토큰에서 추출한 사용자 이름이 예상 사용자 이메일과 일치하고, 토큰이 만료되지 않았는지 확인
     * 토큰 만료 시 TokenExpiredException을 발생시킴
     *
     * @param token 유효성을 검증할 대상 JWT 토큰 문자열
     * @param email 토큰의 Subject와 비교할 예상 사용자 이메일
     * @return 토큰이 유효하면 true, 그렇지 않으면 (사용자 이름 불일치 시) false를 반환
     * @throws TokenExpiredException 토큰이 만료되었을 경우
     * @throws Exception extractUsername 또는 isTokenExpired 내부에서 발생할 수 있는 다른 예외
     */

    public boolean validateToken(String token, String email) {
        final String username = extractUsername(token);
        if (isTokenExpired(token)) {
            throw new TokenExpiredException("Token has expired");
        }
        return (username.equals(email) && !isTokenExpired(token));
    }

}
