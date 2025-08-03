package com.example.CineHive.domain.mail.service;

import org.springframework.scheduling.annotation.Async;

public interface EmailService {

    /**
     * 비밀번호 재설정 이메일을 비동기적으로 발송합니다.
     * @param to 수신자 이메일 주소
     * @param selector 사용자가 링크를 통해 다시 보낼 조회용 토큰
     * @param validator 사용자가 링크를 통해 다시 보낼 검증용 평문 토큰
     */
    @Async
    void sendPasswordResetEmail(String to, String selector, String validator);

    /**
     * 계정 잠금 알림 이메일을 비동기적으로 발송합니다.
     * @param to 수신자 이메일 주소
     */
    @Async
    void sendAccountLockoutEmail(String to);
}