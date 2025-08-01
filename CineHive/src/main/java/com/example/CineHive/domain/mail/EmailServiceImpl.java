package com.example.CineHive.domain.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    /**
     * 비밀번호 재설정 이메일을 비동기적으로 발송합니다.
     * (현재는 실제 발송 대신 로그를 출력하는 Stub 구현입니다.)
     * @param to 수신자 이메일 주소
     * @param selector 사용자가 링크를 통해 다시 보낼 조회용 토큰
     * @param validator 사용자가 링크를 통해 다시 보낼 검증용 평문 토큰
     */
    @Async
    @Override
    public void sendPasswordResetEmail(String to, String selector, String validator) {
        // 프론트엔드와 약속된 재설정 페이지 URL 형식에 맞게 링크를 구성합니다.
        String resetLink = String.format("http://your-frontend-url/reset-password?selector=%s&validator=%s", selector, validator);

        log.info("[Email Stub] 비밀번호 재설정 이메일 발송");
        log.info("수신자: {}", to);
        log.info("재설정 링크: {}", resetLink);
        log.info("========================================");

        try {
            Thread.sleep(2000); // 비동기 처리 및 네트워크 지연 시뮬레이션
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 계정 잠금 알림 이메일 발송 Stub 구현
     * @param to 수신자 이메일 주소
     */
    @Async
    @Override
    public void sendAccountLockoutEmail(String to) {
        // TODO: 실제 이메일 발송 로직 구현
        log.info("[Email Stub] 계정 잠금 알림 이메일 발송");
        log.info("수신자: {}", to);
        log.info("내용: 고객님의 계정이 로그인 연속 실패로 인해 일시적으로 잠겼습니다.");
        log.info("========================================");
    }
}