package com.example.CineHive.domain.auth.controller.entity;

import com.example.CineHive.domain.auth.dto.*;

/**
 * 인증(회원가입, 로그인, 토큰 관리, 중복 확인) 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 */
public interface AuthService {

    /**
     * 새로운 사용자를 등록합니다.
     * @param request 회원가입 정보 DTO
     */
    void register(RegisterRequest request);

    /**
     * 일반 로그인을 처리하고 JWT 토큰(Access/Refresh)을 발급합니다.
     * @param request 로그인 정보 DTO
     * @param userAgent  클라이언트의 User-Agent 정보
     * @return 로그인 응답 DTO (토큰 및 사용자 정보 포함)
     */
    LoginResponse login(LoginRequest request, String userAgent);

    /**
     * Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 재발급합니다. (토큰 로테이션)
     * @param request 재발급 요청 DTO (Refresh Token 포함)
     * @return 재발급된 토큰 정보 DTO
     */
    ReissueTokenResponse reissueToken(ReissueTokenRequest request);

    /**
     * 로그아웃을 처리합니다. Redis에 저장된 Refresh Token을 삭제합니다.
     * @param userEmail 로그아웃할 사용자의 이메일
     */
    void logout(String userEmail);

    /**
     * 이메일 사용 가능 여부를 확인합니다.
     * @param email 확인할 이메일
     * @return 사용 가능 시 true
     */
    boolean isEmailAvailable(String email);

    /**
     * 닉네임 사용 가능 여부를 확인합니다.
     * @param nickname 확인할 닉네임
     * @return 사용 가능 시 true
     */
    boolean isNicknameAvailable(String nickname);

    /**
     * 비밀번호 재설정 토큰을 생성하고, 사용자에게 이메일로 발송합니다.
     * @param request 비밀번호 찾기 요청 DTO (이메일 포함)
     * @param clientIp 요청한 클라이언트의 IP 주소
     */
    void createPasswordResetToken(ForgotPasswordRequest request, String clientIp);

    /**
     * 제공된 토큰을 검증하고, 유효한 경우 새로운 비밀번호로 재설정합니다.
     * @param request 비밀번호 재설정 DTO (토큰, 새 비밀번호 포함)
     */
    void resetPassword(ResetPasswordRequest request);
}