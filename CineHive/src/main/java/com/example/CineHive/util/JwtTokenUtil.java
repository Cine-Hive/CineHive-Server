package com.example.CineHive.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HTTP 요청에서 JWT 토큰을 안전하게 추출하고, 토큰으로부터 필요한 정보(예: 사용자 이메일)를* 추출하는 유틸리티 클래스. 웹 요청 처리 시 JWT 인증 과정에서 사용
 */
@Component
public class JwtTokenUtil {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * HTTP 요청 객체(HttpServletRequest)로부터 'Authorization' 헤더에 담긴 JWT(JSON Web Token) 토큰을 추출하는 메서드
     * 일반적으로 JWT는 'Authorization: Bearer [토큰값]' 형식으로 전송되며, 이 메서드는 'Bearer ' 접두사를 확인하고 제거한 순수한 토큰 문자열을 반환
     * @param request HTTP 요청 객체 (클라이언트로부터 받은 요청)
     * @return 'Authorization' 헤더에서 추출된 순수한 JWT 토큰 문자열. 헤더가 없거나
     *         'Bearer ' 형식으로 시작하지 않으면 null을 반환
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * 순수한 JWT 토큰 문자열로부터 토큰 내부에 담긴 사용자 이메일(Subject 클레임)을 추출하는 메서드
     * 이 메서드는 실제 추출 로직을 내부적으로 사용되는 JwtUtil 객체에 위임
     *
     * @param token 추출 대상인 순수한 JWT 토큰 문자열
     * @return 토큰에서 추출된 사용자 이메일 (String). 토큰이 유효하지 않거나 이메일 정보가 없으면 예외 발생 가능 (JwtUtil 구현에 따름).
     */
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }
}
