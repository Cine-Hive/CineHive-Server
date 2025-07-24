package com.example.CineHive.service.auth;

import com.example.CineHive.dto.auth.LoginRequest;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.auth.RegisterRequest;

/**
 * 인증(회원가입, 로그인) 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 */
public interface AuthService {

    /**
     * 새로운 사용자를 등록합니다.
     * @param request 회원가입 정보 DTO
     */
    void register(RegisterRequest request);

    /**
     * 일반 로그인을 처리하고 JWT 토큰을 발급합니다.
     * @param request 로그인 정보 DTO
     * @param userAgent  클라이언트의 User-Agent 정보
     * @return 로그인 응답 DTO (토큰 및 사용자 정보 포함)
     */
    LoginResponse login(LoginRequest request, String userAgent);
}