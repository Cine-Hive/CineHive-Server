package com.example.CineHive.global.scheduler;

import com.example.CineHive.domain.auth.password.repository.PasswordHistoryRepository;
import com.example.CineHive.domain.auth.password.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;

    /**
     * 매일 오전 4시에 실행되어 만료된 비밀번호 재설정 토큰을 삭제합니다.
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void purgeExpiredResetTokens() {
        log.info("만료된 비밀번호 재설정 토큰 정리 작업을 시작합니다.");
        try {
            passwordResetTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
            log.info("만료된 비밀번호 재설정 토큰 정리 작업이 완료되었습니다.");
        } catch (Exception e) {
            log.error("만료된 재설정 토큰 정리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 매월 1일 오전 5시에 실행되어 1년 이상된 비밀번호 히스토리를 삭제합니다.
     */
    @Scheduled(cron = "0 0 5 1 * *")
    @Transactional
    public void purgeOldPasswordHistory() {
        log.info("오래된 비밀번호 히스토리 정리 작업을 시작합니다.");
        try {
            Instant oneYearAgo = Instant.now().minus(365, ChronoUnit.DAYS);
            passwordHistoryRepository.deleteAllByCreatedAtBefore(oneYearAgo);
            log.info("1년 이상된 비밀번호 히스토리 정리 작업이 완료되었습니다.");
        } catch (Exception e) {
            log.error("오래된 비밀번호 히스토리 정리 중 오류가 발생했습니다.", e);
        }
    }
}