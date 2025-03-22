package com.example.CineHive.util;

/*
    // HTTP 요청에서 JWT 토큰을 추출하는 메서드
 */

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {
    @Autowired
    private JwtUtil jwtUtil;

    /*
    HTTP 요청에서 JWT 토큰을 추출
    */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // "Bearer " 이후의 토z큰 부분 추출
        }
        return null;
    }

    /*
    JWT 토큰에서 사용자 이메일 추출
    */
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }
}
