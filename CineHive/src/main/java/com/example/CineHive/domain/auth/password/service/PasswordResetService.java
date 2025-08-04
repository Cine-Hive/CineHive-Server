package com.example.CineHive.domain.auth.password.service;

import com.example.CineHive.domain.auth.password.dto.ForgotPasswordRequest;
import com.example.CineHive.domain.auth.password.dto.ResetPasswordRequest;

/**
 * 비밀번호 찾기 및 재설정 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface PasswordResetService {
    /**
     * 비밀번호 재설정 토큰을 생성하고 사용자에게 이메일로 발송합니다.
     * @param request 사용자의 이메일이 담긴 DTO
     * @param clientIp 요청한 클라이언트의 IP 주소 (요청 제한용)
     */
    void createPasswordResetToken(ForgotPasswordRequest request, String clientIp);

    /**
     * 재설정 토큰을 사용하여 사용자의 비밀번호를 새로 설정합니다.
     * @param request 재설정 토큰과 새 비밀번호 정보가 담긴 DTO
     */
    void resetPassword(ResetPasswordRequest request);
}