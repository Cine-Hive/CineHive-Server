package com.example.CineHive.domain.auth.password.service;

import com.example.CineHive.domain.auth.password.dto.ForgotPasswordRequest;
import com.example.CineHive.domain.auth.password.dto.ResetPasswordRequest;
import com.example.CineHive.domain.auth.password.entity.PasswordHistory;
import com.example.CineHive.domain.auth.password.entity.PasswordResetToken;
import com.example.CineHive.domain.auth.password.repository.PasswordHistoryRepository;
import com.example.CineHive.domain.auth.password.repository.PasswordResetTokenRepository;
import com.example.CineHive.domain.mail.service.EmailService;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.example.CineHive.global.properties.SecurityPolicyProperties;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.properties.PasswordResetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    // Repositories
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;

    // Services & Components
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    // Configuration Properties
    private final PasswordResetProperties passwordResetProperties;
    private final SecurityPolicyProperties securityPolicy;

    // Constants
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    private static final String FORGOT_PASSWORD_EMAIL_KEY_PREFIX = "rate-limit:forgot-password:email:";
    private static final String FORGOT_PASSWORD_IP_KEY_PREFIX = "rate-limit:forgot-password:ip:";


    @Override
    @Transactional
    public void createPasswordResetToken(ForgotPasswordRequest request, String clientIp) {
        validateRateLimit(request.email(), clientIp);

        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String selector = generateRandomBase64(16);
            String validator = generateRandomBase64(32);
            String validatorHash = passwordEncoder.encode(validator);
            Instant expiryDate = Instant.now().plus(passwordResetProperties.getTokenExpiry());

            PasswordResetToken resetToken = passwordResetTokenRepository.findByUser(user).orElse(null);

            if (resetToken != null) {
                resetToken.updateToken(selector, validatorHash, expiryDate);
            } else {
                resetToken = new PasswordResetToken(user, selector, validatorHash, expiryDate);
            }

            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), selector, validator);

            log.debug("비밀번호 재설정 토큰이 생성되어 이메일로 발송되었습니다. 사용자 이메일: {}", user.getEmail());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findBySelector(request.selector())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESET_TOKEN));

        if (token.isExpired()) {
            passwordResetTokenRepository.delete(token);
            throw new BusinessException(ErrorCode.RESET_TOKEN_EXPIRED);
        }

        if (!passwordEncoder.matches(request.validator(), token.getValidatorHash())) {
            throw new BusinessException(ErrorCode.INVALID_RESET_TOKEN);
        }

        User user = token.getUser();

        // 5. 이전 비밀번호 재사용 금지 검증
        validatePasswordHistory(user, request.newPassword());
        archiveOldPassword(user);

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        // 6. 엔티티 상태 관리: @Transactional 덕분에 save()는 선택사항이지만, 명시적으로 유지
        userRepository.save(user);

        passwordResetTokenRepository.delete(token);
        log.info("사용자의 비밀번호가 성공적으로 재설정되었습니다. 사용자 ID: {}", user.getId());
    }

    private void validateRateLimit(String email, String clientIp) {
        String emailKey = FORGOT_PASSWORD_EMAIL_KEY_PREFIX + email;
        String ipKey = FORGOT_PASSWORD_IP_KEY_PREFIX + clientIp;

        if (isRateLimited(emailKey, securityPolicy.getRateLimit().getForgotPassword().getEmailWindow())) {
            log.warn("비밀번호 재설정 요청 횟수 초과 (이메일 기준): {}", email);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
        if (isRateLimited(ipKey, securityPolicy.getRateLimit().getForgotPassword().getIpWindow())) {
            log.warn("비밀번호 재설정 요청 횟수 초과 (IP 기준): {}", clientIp);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private boolean isRateLimited(String key, Duration window) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, "locked", window) == Boolean.FALSE;
        } catch (Exception e) {
            log.error("Redis Rate Limiting 중 오류 발생", e);
            // Redis 장애 시 요청을 막지 않도록 안전하게 true 반환 (오류 로깅 후)
            return false;
        }
    }

    private void validatePasswordHistory(User user, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        history.stream()
                .limit(securityPolicy.getPassword().getHistorySize())
                .forEach(record -> {
                    if (passwordEncoder.matches(newPassword, record.getPasswordHash())) {
                        throw new BusinessException(ErrorCode.PASSWORD_REUSE_PROHIBITED);
                    }
                });
    }

    private void archiveOldPassword(User user) {
        PasswordHistory newHistoryRecord = new PasswordHistory(user, user.getPassword());
        passwordHistoryRepository.save(newHistoryRecord);

        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        if (history.size() > securityPolicy.getPassword().getHistorySize()) {
            PasswordHistory oldestRecord = history.get(history.size() - 1);
            passwordHistoryRepository.delete(oldestRecord);
        }
    }

    private String generateRandomBase64(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
