package com.example.CineHive.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
1. 라인  24 ~38번째 줄은 HEADER, PAYLOAD, SIGNATURE를 생성하고 인코딩하는 부분
   JWT의 HEADER는 Jwts.builder() 메서드에서 자동으로 생성되며, 기본적으로 alg(알고리즘)와 typ(타입) 정보를 포함하는데 이는 jwts.builder()에서 내부적으로 처리한다.
   이때 Jwts.builder() 메서드를 사용하여 JWT를 생성할 때,  내부적으로 HEADER와 PAYLOAD를 JSON으로 변환하고 Base64Url로 인코딩 하는데,
   JJWT 라이브러리에 의해 자동으로 처리
2. createToken 함수의 .signWith(SignatureAlgorithm.HS256, SECRET_KEY)에서 SIGNATURE를 생성한다. 이 부분에서 비밀 키를 사용하여 HMAC_SHA256 알고리즘으로 해싱한다.
3. createToken 함수에서 compact에서 HEADER, PAYLOAD, SIGNATURE를 점(.)으로 연결하여 최종 JWT 문자열을 생성

요약 하자면,
    (1) HEADER: Jwts.builder()에서 자동 생성.
    (2) PAYLOAD: claims와 subject를 통해 설정.
    (3)SIGNATURE: signWith 메서드에서 HMAC_SHA256 알고리즘으로 생성.
    (4)Base64Url 인코딩: JJWT 라이브러리에서 자동 처리.
 */
@Component
@Slf4j
public class JwtUtil {

    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    private final long EXPIRATION_TIME = 1000 * 60 * 60;

    public String generateToken(String email) {
        log.info("Generating token for email: {}", email);
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        try {

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(SECRET_KEY)
                    .compact();

            log.info("Generated JWT: {}", token);
            return token;
        } catch (Exception e) {
            log.error("Error generating JWT: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, String email) {
        final String username = extractUsername(token);
        return (username.equals(email) && !isTokenExpired(token));
    }
}