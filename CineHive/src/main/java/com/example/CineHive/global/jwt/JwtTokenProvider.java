package com.example.CineHive.global.jwt;

import com.example.CineHive.global.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT(JSON Web Token) 생성, 검증, 정보 추출 등 토큰 관련 모든 기능을 담당하는 클래스입니다.
 * Spring Security 인증 과정 전반에서 사용됩니다.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    /**
     * JwtTokenProvider 생성자입니다.
     * JwtProperties를 주입받아 JWT 시크릿 키와 토큰 만료 시간을 초기화합니다.
     *
     * @param jwtProperties application.yml의 'app.jwt' 설정을 담고 있는 객체
     */
    public JwtTokenProvider(JwtProperties jwtProperties) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMillis = jwtProperties.getAccessTokenExpiration().toMillis();
        this.refreshTokenExpirationMillis = jwtProperties.getRefreshTokenExpiration().toMillis();
    }

    /**
     * 사용자의 이메일을 기반으로 Access Token을 생성합니다.
     *
     * @param email 토큰의 주체(subject)가 될 사용자의 이메일
     * @return 생성된 Access Token 문자열
     */
    public String createAccessToken(String email) {
        return createToken(email, accessTokenExpirationMillis);
    }

    /**
     * 사용자의 이메일을 기반으로 Refresh Token을 생성합니다.
     *
     * @param email 토큰의 주체(subject)가 될 사용자의 이메일
     * @return 생성된 Refresh Token 문자열
     */
    public String createRefreshToken(String email) {
        return createToken(email, refreshTokenExpirationMillis);
    }

    /**
     * JWT 토큰을 실제로 생성하는 내부 메서드입니다.
     *
     * @param subject      토큰의 주체 (사용자 이메일)
     * @param expirationMs 토큰의 만료 시간 (밀리초 단위)
     * @return 생성된 JWT 토큰 문자열
     */
    private String createToken(String subject, long expirationMs) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(this.key)
                .compact();
    }

    /**
     * 토큰에서 사용자 이메일(Subject)을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 추출된 사용자 이메일
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     *
     * @param token JWT 토큰
     * @return 만료되었다면 true, 그렇지 않으면 false
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (Exception e) {
            log.warn("토큰 검증 중 오류 발생: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 토큰의 유효성을 검증합니다. (사용자 이름 일치 여부 + 만료 여부)
     *
     * @param token    JWT 토큰
     * @param username 검증할 사용자 이름
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (username.equals(extractedUsername) && !isTokenExpired(token));
    }

    /**
     * 토큰에서 모든 클레임(Payload)을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 추출된 클레임 객체
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰에서 특정 클레임을 추출하는 제네릭 메서드입니다.
     *
     * @param token          JWT 토큰
     * @param claimsResolver 클레임에서 원하는 정보를 추출하는 함수
     * @param <T>            추출할 정보의 타입
     * @return 추출된 정보
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * HTTP 요청의 'Authorization' 헤더에서 'Bearer ' 접두사를 제거하고 순수한 토큰 문자열을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 토큰 문자열. 없거나 형식이 맞지 않으면 null을 반환.
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
