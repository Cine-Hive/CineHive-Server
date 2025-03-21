package com.example.CineHive.util;

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

    public boolean validateToken(String token, String email) {
        final String username = extractUsername(token);
        return (username.equals(email) && !isTokenExpired(token));
    }
}
